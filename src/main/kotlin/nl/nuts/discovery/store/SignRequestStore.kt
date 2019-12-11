package nl.nuts.discovery.store

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.service.SignRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.security.cert.X509Certificate

/**
 * Interface which describes how a SignRequestStore should behave.
 */
interface SignRequestStore {
    /**
     * clear all signed nodes and pending requests. Needed for testing purposes
     */
    fun clearAll()

    /**
     * Get all signed certificates
     */
    fun signedCertificates(): List<SignRequest>

    /**
     * Retrieve a signed certificate based on the name or null if not found.
     */
    fun signedCertificate(serial: CordaX500Name): X509Certificate?

    /**
     * Get all pending signing requests
     */
    fun pendingSignRequests(): List<SignRequest>

    /**
     * Retrieve a pending certificateRequest based on the name. Returns null if not found.
     */
    fun pendingSignRequest(serial: CordaX500Name): SignRequest?

    /**
     * Mark a SignRequest as signed
     */
    fun markAsSigned(request: SignRequest)

    /**
     * Add a new CSR to the store
     */
    fun addSigningRequest(request: PKCS10CertificationRequest) : SignRequest
}