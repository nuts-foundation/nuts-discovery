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

import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.signWithCert
import net.corda.core.node.NetworkParameters
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.X509Utilities
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkParameters
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
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
import javax.annotation.PostConstruct

@Configuration
@ConfigurationProperties("nuts.discovery")
data class NutsDiscoveryProperties(
    var rootCertPath: String = "",
    var intermediateCertPath: String = "",
    var intermediateKeyPath: String = "",
    var networkMapCertPath: String = "",
    var networkMapKeyPath: String = ""
)

/**
 * A naive implementation for the {@link CertificateAndKeyService}
 */
@Profile(value = arrayOf("dev", "test", "default"))
@Service
class LocalCertificateAndKeyService : CertificateAndKeyService {

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    // map of signed certificates
    lateinit var certificates: MutableMap<CordaX500Name, SignRequest>
    // map with pending signing requests
    lateinit var signRequests: MutableMap<CordaX500Name, SignRequest>

    @PostConstruct
    fun init() {
        certificates = mutableMapOf()
        signRequests = mutableMapOf()
    }

    override fun submitSigningRequest(request: PKCS10CertificationRequest) {
        val name = CordaX500Name.parse(request.subject.toString())
        signRequests[name] = SignRequest(request)
    }

    override fun signCertificate(serial: CordaX500Name): X509Certificate? {
        val request = signRequests[serial] ?: return null

        val pkcs10 = JcaPKCS10CertificationRequest(request.request)
        val name = CordaX500Name.parse(request.request!!.subject.toString())
        val nodeCaCert = X509Utilities.createCertificate(
            CertificateType.NODE_CA,
            intermediateCertificate(),
            intermediateKeyPair(),
            name.x500Principal,
            pkcs10.publicKey,
            nameConstraints = null)

        request.certificate = nodeCaCert
        request.approved = true

        // Add cert to signed certificates
        certificates[name] = request

        // remove csr from pending requests
        signRequests.remove(serial)
        return nodeCaCert
    }

    override fun signedCertificate(serial: CordaX500Name): X509Certificate? {
        return certificates[serial]?.certificate
    }

    override fun pendingCertificate(serial: CordaX500Name): PKCS10CertificationRequest? {
        return signRequests[serial]?.request
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
            Pair(::intermediateKeyPair, "intermediate key"),
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

    private fun networkMapKey(): PrivateKey {
        val reader = PemReader(Files.newBufferedReader(loadResourceWithNullCheck(nutsDiscoveryProperties.networkMapKeyPath)) as Reader?)
        val key = reader.readPemObject()

        reader.close()

        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
        return kf.generatePrivate(PKCS8EncodedKeySpec(key.content))
    }

    private fun intermediateKeyPair(): KeyPair {
        val keyReader = PemReader(Files.newBufferedReader(loadResourceWithNullCheck(nutsDiscoveryProperties.intermediateKeyPath)))
        val key = keyReader.readPemObject()

        keyReader.close()

        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
        val priKey = kf.generatePrivate(PKCS8EncodedKeySpec(key.content))

        return KeyPair(intermediateCertificate().publicKey, priKey)
    }

    override fun clearAll() {
        certificates.clear()
        signRequests.clear()
    }

    override fun pendingSignRequests(): List<SignRequest> {
        return signRequests.values.toList()
    }

    override fun signedCertificates(): List<SignRequest> {
        return certificates.values.toList()
    }
}