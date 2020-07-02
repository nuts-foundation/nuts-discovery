package nl.nuts.discovery.api

import nl.nuts.discovery.model.CertificateSigningRequest
import nl.nuts.discovery.model.CertificateWithChain

interface CertificatesApiService {

    fun listCertificates(otherName: String): List<CertificateWithChain>

    fun listRequests(otherName: String): List<CertificateSigningRequest>

    fun submit(body: String): CertificateSigningRequest
}
