/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2020 Nuts community
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

import nl.nuts.discovery.api.CertificatesApiService
import nl.nuts.discovery.model.CertificateSigningRequest
import nl.nuts.discovery.model.CertificateWithChain
import nl.nuts.discovery.model.CertificateRequest
import nl.nuts.discovery.store.NutsCertificateRequestRepository
import nl.nuts.discovery.store.entity.NutsCertificateRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CertificatesApiServiceImpl : CertificatesApiService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    override fun listCertificates(otherName: String): List<CertificateWithChain> {
        TODO("Not yet implemented")
    }

    override fun listRequests(otherName: String): List<CertificateSigningRequest> {
        TODO("Not yet implemented")
    }

    override fun submit(body: String): CertificateSigningRequest {
        val nutsCertificateRequest = NutsCertificateRequest.fromPEM(body)

        logger.info("Received request for: ${nutsCertificateRequest.name}, oid: ${nutsCertificateRequest.oid}")

        nutsCertificateRequestRepository.save(nutsCertificateRequest)
        if (nutsDiscoveryProperties.autoAck) {
             //todo sign
        }

        return CertificateSigningRequest(
            subject = nutsCertificateRequest.name!!,
            pem = nutsCertificateRequest.pem!!,
            submittedAt = nutsCertificateRequest.submittedAt.toString()
        )
    }
}