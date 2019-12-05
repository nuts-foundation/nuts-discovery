package nl.nuts.discovery.store

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.service.SignRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.springframework.stereotype.Service
import java.security.cert.X509Certificate

@Service
class InMemorySignRequestStore : SignRequestStore {

    // map of signed certificates
    val certificates = mutableMapOf<CordaX500Name, SignRequest>()

    // map with pending signing requests
    var signRequests = mutableMapOf<CordaX500Name, SignRequest>()

    override fun clearAll() {
        certificates.clear()
        signRequests.clear()
    }

    override fun markAsSigned(request: SignRequest) {
        val name = request.legalName()
        signRequests.remove(name)
        certificates[name] = request
    }

    override fun addSigningRequest(request: PKCS10CertificationRequest) : SignRequest {
        val name = CordaX500Name.parse(request.subject.toString())
        // TODO: Check if the name is already in use and if it is OK to overwrite.
        val signRequest = SignRequest(request)
        signRequests[name] = signRequest
        return signRequest
    }

    override fun signedCertificate(serial: CordaX500Name): X509Certificate? {
        return certificates[serial]?.certificate
    }

    override fun pendingSignRequest(serial: CordaX500Name): SignRequest? {
        return signRequests[serial]
    }

    override fun pendingSignRequests(): List<SignRequest> {
        return signRequests.values.toList()
    }

    override fun signedCertificates(): List<SignRequest> {
        return certificates.values.toList()
    }
}