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
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.X509Utilities
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.file.Files
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
 * A naive implementation for the {@link CertificateAndKeyService} which will auto-sign all requests
 */
@Profile(value = arrayOf("dev", "test", "default"))
@Service
class LocalCertificateAndKeyService : CertificateAndKeyService {

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    lateinit var certificates: MutableMap<CordaX500Name, X509Certificate>

    @PostConstruct
    fun init() {
        certificates = mutableMapOf()
    }

    override fun submitSigningRequest(request: PKCS10CertificationRequest) {
        val request = JcaPKCS10CertificationRequest(request)
        val name = CordaX500Name.parse(request.subject.toString())
        val nodeCaCert = X509Utilities.createCertificate(
                CertificateType.NODE_CA,
                intermediateCertificate(),
                intermediateKeyPair(),
                name.x500Principal,
                request.publicKey,
                nameConstraints = null)

        certificates[name] = nodeCaCert
    }

    override fun signedCertificate(serial: CordaX500Name) : X509Certificate? {
        return certificates[serial]
    }

    override fun rootCertificate() : X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(Paths.get(this.javaClass.classLoader.getResource(nutsDiscoveryProperties.rootCertPath).toURI()))
    }

    override fun networkMapCertificate() : X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(Paths.get(this.javaClass.classLoader.getResource(nutsDiscoveryProperties.networkMapCertPath).toURI()))
    }

    override fun intermediateCertificate() : X509Certificate {
        return X509Utilities.loadCertificateFromPEMFile(Paths.get(this.javaClass.classLoader.getResource(nutsDiscoveryProperties.intermediateCertPath).toURI()))
    }

    private fun networkMapKey() : PrivateKey {
        val reader = PemReader(Files.newBufferedReader(Paths.get(this.javaClass.classLoader.getResource(nutsDiscoveryProperties.networkMapKeyPath).toURI())))
        val key = reader.readPemObject()

        reader.close()

        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
        return kf.generatePrivate(PKCS8EncodedKeySpec(key.content))
    }

    private fun intermediateKeyPair() : KeyPair {
        val keyReader = PemReader(Files.newBufferedReader(Paths.get(this.javaClass.classLoader.getResource(nutsDiscoveryProperties.intermediateKeyPath).toURI())))
        val key = keyReader.readPemObject()

        keyReader.close()

        val kf = KeyFactory.getInstance("RSA") // or "EC" or whatever
        val priKey = kf.generatePrivate(PKCS8EncodedKeySpec(key.content))

        return KeyPair(intermediateCertificate().publicKey, priKey)
    }
}