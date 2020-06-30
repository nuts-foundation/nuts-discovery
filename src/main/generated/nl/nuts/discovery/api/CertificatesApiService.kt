package nl.nuts.discovery.api


interface CertificatesApiService {

    fun listCertificates(urn: String): List<String>

    fun submit(body: String): Unit
}
