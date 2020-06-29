package nl.nuts.discovery.api


interface CertificatesApiService {

    fun submitCertificateSigningRequest(body: String): Unit
}
