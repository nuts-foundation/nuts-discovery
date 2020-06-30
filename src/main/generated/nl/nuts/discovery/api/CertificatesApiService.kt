package nl.nuts.discovery.api

import nl.nuts.discovery.model.CertificateRequest

interface CertificatesApiService {

    fun listCertificates(urn: String): List<String>

    fun listRequests(urn: String): List<CertificateRequest>

    fun submit(body: String): CertificateRequest
}
