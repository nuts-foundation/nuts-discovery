package nl.nuts.discovery.api

import nl.nuts.discovery.model.CertificateRequest
import nl.nuts.discovery.model.CertificateWithChain

interface CertificatesApiService {

    fun listCertificates(urn: String): List<CertificateWithChain>

    fun listRequests(urn: String): List<CertificateRequest>

    fun submit(body: String): CertificateRequest
}
