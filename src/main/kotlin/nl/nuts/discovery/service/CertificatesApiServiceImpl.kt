/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2020 Nuts community
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package nl.nuts.discovery.service

import net.corda.core.internal.copyTo
import net.corda.nodeapi.internal.crypto.X509Utilities
import nl.nuts.discovery.api.CertificatesApiService
import nl.nuts.discovery.model.CertificateSigningRequest
import nl.nuts.discovery.model.CertificateWithChain
import nl.nuts.discovery.store.CertificateRepository
import nl.nuts.discovery.store.NutsCertificateRequestRepository
import nl.nuts.discovery.store.entity.Certificate
import nl.nuts.discovery.store.entity.NutsCertificateRequest
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.X509KeyUsage
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sun.security.provider.X509Factory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.regex.Pattern
import javax.security.auth.x500.X500Principal

@Service
class CertificatesApiServiceImpl : CertificatesApiService, CertificateSigningService {

    companion object {
        /**
         * Check both file path on disk and in resources (test)
         */
        fun loadResourceWithNullCheck(location: String): Path {

            if (File(location).exists()) {
                return Paths.get(File(location).toURI())
            }

            val resource = javaClass.classLoader.getResource(location)
                ?: throw IllegalArgumentException("resource not found at $location")

            val uri = resource.toURI()
            return Paths.get(uri)
        }

        /**
         * create a single PEM string from given paths
         *
         * @param paths locations of PEM files, starting with lowest CA and ending with the root
         */
        fun chainAsPEM(paths: Array<Path>) : String {
            val out = ByteArrayOutputStream()

            for ((index, it) in paths.withIndex()) {
                it.copyTo(out)
                if (index < paths.size - 1) {
                    out.write("\n".toByteArray())
                }
            }

            return out.toString(Charsets.UTF_8.name())
        }
    }

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    @Autowired
    lateinit var certificateRepository: CertificateRepository

    override fun listCertificates(otherName: String): List<CertificateWithChain> {
        return certificateRepository.findByOid(otherName).map {
            CertificateWithChain(it.toPem(), splitChain(it.chain))
        }
    }

    override fun listRequests(otherName: String): List<CertificateSigningRequest> {
        return nutsCertificateRequestRepository.findByOid(otherName).map {
            entityToApiModel(it)
        }
    }

    override fun submit(body: String): CertificateSigningRequest {
        val nutsCertificateRequest = NutsCertificateRequest.fromPEM(body)

        logger.info("Received request for: ${nutsCertificateRequest.name}, oid: ${nutsCertificateRequest.oid}")

        nutsCertificateRequestRepository.save(nutsCertificateRequest)
        if (nutsDiscoveryProperties.autoAck) {
             sign(nutsCertificateRequest)
        }

        return entityToApiModel(nutsCertificateRequest)
    }

    private fun entityToApiModel(nutsCertificateRequest: NutsCertificateRequest) : CertificateSigningRequest {
        return CertificateSigningRequest(
            subject = nutsCertificateRequest.name!!,
            pem = nutsCertificateRequest.pem!!,
            submittedAt = nutsCertificateRequest.submittedAt.toString()
        )
    }

    override fun sign(request: NutsCertificateRequest) : X509Certificate {
        val pkcs10 = JcaPKCS10CertificationRequest(request.toPKCS10())

        val pair = caKeyPair()
        val issuer = caCertificate()

        if (request.oid == null) {
            throw java.lang.IllegalArgumentException("missing oid")
        }

        // todo validity periods
        // todo serial number generation: configure a salt, use an internal seq number (count on certs), append them then sha2
        val certificateBuilder = JcaX509v3CertificateBuilder(
            X500Name(issuer.subjectX500Principal.getName(X500Principal.RFC1779)),
            BigInteger("1"),
            Date(System.currentTimeMillis()),
            Date(System.currentTimeMillis() + (3L * 365 * 24 * 60 * 60 * 1000)),
            pkcs10.subject,
            pair.public
        ).addExtension(
            Extension.basicConstraints,
            false, // non-critical
            BasicConstraints(true) // isCa
        ).addExtension(
            Extension.keyUsage,
            true,
            X509KeyUsage(
                X509KeyUsage.digitalSignature or
                X509KeyUsage.keyCertSign   or
                X509KeyUsage.cRLSign
            )
        )

        val oidSeq = DLSequence(arrayOf(NutsCertificateRequest.NUTS_VENDOR_EXTENSION, DERUTF8String(request.oid)))
        val names = mutableListOf(GeneralName(GeneralName.otherName, oidSeq))

        val emailAttr = pkcs10.getAttributes(BCStyle.EmailAddress)
        if (emailAttr != null && emailAttr.isNotEmpty()) {
            val emailASN1String = emailAttr.first().attrValues.getObjectAt(0) as ASN1String
            val email = emailASN1String.string

            names.add(GeneralName(GeneralName.rfc822Name, email))
        }

        val subjectAltNames = GeneralNames(names.toTypedArray())
        certificateBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames)

        val sigGen = JcaContentSignerBuilder("SHA384WITHECDSA").build(pair.private)

        val holder: X509CertificateHolder = certificateBuilder.build(sigGen)
        val x509CertificateStructure = holder.toASN1Structure()

        // Read Certificate
        val cf = CertificateFactory.getInstance("X.509", "BC")
        val is1: InputStream = ByteArrayInputStream(x509CertificateStructure.encoded)
        val theCert = cf.generateCertificate(is1) as X509Certificate
        is1.close()

        certificateRepository.save(Certificate.fromX509Certificate(theCert, chain()))
        nutsCertificateRequestRepository.delete(request)

        return theCert

        // todo validation?
    }

    private fun splitChain(cChain: String?) : List<String> {
        val cList = mutableListOf<String>()

        if (cChain != null) {
            val pBegin = Pattern.compile(X509Factory.BEGIN_CERT)
            val pEnd = Pattern.compile(X509Factory.END_CERT)
            val mStart = pBegin.matcher(cChain)
            val mEnd = pEnd.matcher(cChain)

            while(mEnd.find() && mStart.find()) {
                cList.add(cChain.substring(mStart.start(), mEnd.end()))
            }
        }

        return cList
    }

    private fun chain() : String {
        val rootPath = loadResourceWithNullCheck(nutsDiscoveryProperties.nutsRootCertPath)
        val caPath = loadResourceWithNullCheck(nutsDiscoveryProperties.nutsCACertPath)

        return chainAsPEM(arrayOf(caPath, rootPath))
    }

    /**
     * returns the Nuts CA certificate
     */
    fun caCertificate(): X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(loadResourceWithNullCheck(nutsDiscoveryProperties.nutsCACertPath))
    }

    /**
     * Reads the key from disk and returns a PrivateKey instance, expects PKCS8 encoded EC key
     */
    fun caKey(): PrivateKey {
        val reader = PemReader(Files.newBufferedReader(loadResourceWithNullCheck(nutsDiscoveryProperties.nutsCAKeyPath)) as Reader?)
        val key = reader.readPemObject()

        reader.close()

        val kf = KeyFactory.getInstance("EC")
        return kf.generatePrivate(PKCS8EncodedKeySpec(key.content))
    }

    fun caKeyPair(): KeyPair {
        return KeyPair(caCertificate().publicKey, caKey())
    }
}