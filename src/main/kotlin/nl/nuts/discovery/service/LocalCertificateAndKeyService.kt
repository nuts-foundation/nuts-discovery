/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2019 Nuts community
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

import net.corda.core.crypto.Crypto
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.signWithCert
import net.corda.core.node.NetworkParameters
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.ContentSignerBuilder
import net.corda.nodeapi.internal.crypto.X509Utilities
import net.corda.nodeapi.internal.crypto.toJca
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkParameters
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.io.Reader
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.annotation.PostConstruct


/**
 * A naive implementation for the {@link CertificateAndKeyService}
 */
@Profile(value = arrayOf("dev", "test", "default"))
@Service
class LocalCertificateAndKeyService : CertificateAndKeyService {

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    /**
     * Create a certificate, add the email extension with email from the request
     * and sign it with the Nuts intermediate keyPair.
     */
    override fun signCertificate(request: PKCS10CertificationRequest): X509Certificate {
        val pkcs10 = JcaPKCS10CertificationRequest(request)
        val name = CordaX500Name.parse(request.subject.toString())

        val issuerCertificate = intermediateCertificate()
        val certificateType = CertificateType.NODE_CA
        val issuer = issuerCertificate.subjectX500Principal
        val issuerKeyPair = intermediateKeyPair()
        val subject = name.x500Principal
        val subjectPublicKey = pkcs10.publicKey
        val validityWindow = X509Utilities.DEFAULT_VALIDITY_WINDOW
        val window = X509Utilities.getCertificateValidityWindow(validityWindow.first, validityWindow.second, issuerCertificate)

        val signatureScheme = Crypto.findSignatureScheme(issuerKeyPair.private)
        val provider = Crypto.findProvider(signatureScheme.providerName)
        val signer = ContentSignerBuilder.build(signatureScheme, issuerKeyPair.private, provider)
        val builder = X509Utilities.createPartialCertificate(
            certificateType,
            issuer,
            issuerKeyPair.public,
            subject,
            subjectPublicKey,
            window,
            null,
            null,
            null)


        val emailAttr = request.getAttributes(BCStyle.EmailAddress)!!.first()
        val emailASN1String = emailAttr!!.attrValues.getObjectAt(0) as ASN1String
        val email = emailASN1String.string

        val subjectAltNames = GeneralNames(GeneralName(GeneralName.rfc822Name, email))
        builder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames)

        return builder.build(signer).run {
            require(isValidOn(Date())) { "Certificate is not valid at instant now" }
            require(isSignatureValid(JcaContentVerifierProviderBuilder().build(issuerKeyPair.public))) { "Invalid signature" }
            toJca()
        }
    }

    override fun rootCertificate(): X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(loadResourceWithNullCheck(nutsDiscoveryProperties.rootCertPath))
    }

    override fun networkMapCertificate(): X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(loadResourceWithNullCheck(nutsDiscoveryProperties.networkMapCertPath))
    }

    override fun intermediateCertificate(): X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(loadResourceWithNullCheck(nutsDiscoveryProperties.intermediateCertPath))
    }

    fun networkMapKey(): PrivateKey {
        val reader = PemReader(Files.newBufferedReader(loadResourceWithNullCheck(nutsDiscoveryProperties.networkMapKeyPath)) as Reader?)
        val key = reader.readPemObject()

        reader.close()

        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
        return kf.generatePrivate(PKCS8EncodedKeySpec(key.content))
    }

    /**
     * Check both file path on disk and in resources (test)
     */
    private fun loadResourceWithNullCheck(location: String): Path {

        if (File(location).exists()) {
            return Paths.get(File(location).toURI())
        }

        val resource = javaClass.classLoader.getResource("$location")
            ?: throw IllegalArgumentException("resource not found at ${location}")

        val uri = resource.toURI()
        return Paths.get(uri)
    }

    private fun intermediateKeyPair(): KeyPair {
        val keyReader = PemReader(Files.newBufferedReader(loadResourceWithNullCheck(nutsDiscoveryProperties.intermediateKeyPath)))
        val key = keyReader.readPemObject()

        keyReader.close()

        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
        val priKey = kf.generatePrivate(PKCS8EncodedKeySpec(key.content))

        return KeyPair(intermediateCertificate().publicKey, priKey)
    }

    override fun signNetworkMap(networkMap: NetworkMap): SignedNetworkMap {
        return networkMap.signWithCert(networkMapKey(), networkMapCertificate())
    }

    override fun signNetworkParams(networkParams: NetworkParameters): SignedNetworkParameters {
        return networkParams.signWithCert(networkMapKey(), networkMapCertificate())
    }

    override fun validate(): List<String> {
        val configProblemSet = mutableMapOf(
            Pair(::rootCertificate, "root certificate"),
            Pair(::intermediateCertificate, "intermediate certificate"),
            Pair(::networkMapCertificate, "network map certificate"),
            Pair(::intermediateCertificate, "intermediate key"),
            Pair(::networkMapKey, "network map key")
        )

        val configProblems = mutableListOf<String>()

        configProblemSet.forEach { f, m ->
            try {
                f.invoke()
            } catch (e: Exception) {
                configProblems.add("Failed to load $m, cause: ${e.message}")
            }
        }

        return configProblems
    }

}