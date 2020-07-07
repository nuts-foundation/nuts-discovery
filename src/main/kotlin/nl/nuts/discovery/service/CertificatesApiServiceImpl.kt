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

import net.corda.nodeapi.internal.crypto.X509Utilities
import nl.nuts.discovery.DiscoveryException
import nl.nuts.discovery.api.CertificatesApiService
import nl.nuts.discovery.model.CertificateSigningRequest
import nl.nuts.discovery.model.CertificateWithChain
import nl.nuts.discovery.store.CertificateRepository
import nl.nuts.discovery.store.CustomCASerialRepository
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
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.X509KeyUsage
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.security.auth.x500.X500Principal
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@Service
class CertificatesApiServiceImpl : AbstractCertificatesService(), CertificatesApiService, CertificateSigningService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    @Autowired
    lateinit var certificateRepository: CertificateRepository

    @Autowired
    lateinit var cASerialRepository: CustomCASerialRepository

    override fun listCertificates(otherName: String): List<CertificateWithChain> {
        return certificateRepository.findByOid(otherName).map {
            CertificateWithChain(it.toPem(), CertificateChain.fromSinglePEM(it.chain).pemEncodedCertificates)
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
        // some checks
        // todo additional checks?
        if (request.oid == null) {
            throw DiscoveryException("missing oid")
        }

        // convert
        val pkcs10 = JcaPKCS10CertificationRequest(request.toPKCS10())

        // x509 builder
        val certificateBuilder = certificateBuilderWithDefaults(pkcs10)
        addCustomExtensions(certificateBuilder, request.oid!!, pkcs10)

        // signature
        val pKey = caKey()
        val sigGen = JcaContentSignerBuilder("SHA384WITHECDSA").build(pKey)
        val holder: X509CertificateHolder = certificateBuilder.build(sigGen)
        val x509CertificateStructure = holder.toASN1Structure()

        // Read Certificate into x509 structure
        val cf = CertificateFactory.getInstance("X.509", "BC")
        val is1: InputStream = ByteArrayInputStream(x509CertificateStructure.encoded)
        val theCert = cf.generateCertificate(is1) as X509Certificate
        is1.close()

        certificateRepository.save(Certificate.fromX509Certificate(theCert, caCertificate().subjectDN.name, chain()))
        nutsCertificateRequestRepository.delete(request)

        return theCert
    }

    private fun addCustomExtensions(builder: X509v3CertificateBuilder, oid: String, pkcs10: JcaPKCS10CertificationRequest) {
        val oidSeq = DLSequence(arrayOf(NutsCertificateRequest.NUTS_VENDOR_EXTENSION, DERUTF8String(oid)))
        val names = mutableListOf(GeneralName(GeneralName.otherName, oidSeq))

        val emailAttr = pkcs10.getAttributes(BCStyle.EmailAddress)
        if (emailAttr != null && emailAttr.isNotEmpty()) {
            val emailASN1String = emailAttr.first().attrValues.getObjectAt(0) as ASN1String
            val email = emailASN1String.string

            names.add(GeneralName(GeneralName.rfc822Name, email))
        }

        val subjectAltNames = GeneralNames(names.toTypedArray())
        builder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames)
    }

    private fun generateSerial(subject: String): BigInteger {
        val caSerial = cASerialRepository.findOrCreateCASerial(subject)
        val certCount = certificateRepository.countByCa(subject)

        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        val text = "${caSerial.salt}${certCount}"

        md.update(text.toByteArray(Charsets.UTF_8))
        val digest: ByteArray = md.digest()

        return BigInteger(digest)
    }

    private fun certificateBuilderWithDefaults(pkcs10: JcaPKCS10CertificationRequest): X509v3CertificateBuilder {
        val issuer = caCertificate()

        val validity: Long = nutsDiscoveryProperties.certificateValidityInDays.toLong() * 24 * 60 * 60 * 1000
        val issuerSubject = issuer.subjectX500Principal.getName(X500Principal.RFC1779)

        return JcaX509v3CertificateBuilder(
            X500Name(issuerSubject),
            generateSerial(issuerSubject),
            Date(System.currentTimeMillis()),
            Date(System.currentTimeMillis() + validity),
            pkcs10.subject,
            caKeyPair().public
        ).addExtension(
            Extension.basicConstraints,
            false, // non-critical
            BasicConstraints(true) // isCa
        ).addExtension(
            Extension.keyUsage,
            true,
            X509KeyUsage(
                X509KeyUsage.digitalSignature or
                    X509KeyUsage.keyCertSign or
                    X509KeyUsage.cRLSign
            )
        )
    }

    private fun chain() : String {
        val rootPath = loadResourceWithNullCheck(nutsDiscoveryProperties.nutsRootCertPath)
        val caPath = loadResourceWithNullCheck(nutsDiscoveryProperties.nutsCACertPath)

        return CertificateChain.fromPaths(arrayOf(caPath, rootPath)).asSinglePEM()
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

    /**
     * Returns the priv + pub key as KeyPair
     */
    fun caKeyPair(): KeyPair {
        return KeyPair(caCertificate().publicKey, caKey())
    }
}