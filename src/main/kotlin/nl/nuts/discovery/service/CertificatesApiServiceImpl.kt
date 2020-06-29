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
import org.springframework.stereotype.Service

@Service
class CertificatesApiServiceImpl : CertificatesApiService {
    override fun listCertificates(otherName: String): List<CertificateWithChain> {
        TODO("Not yet implemented")
    }

    override fun listRequests(otherName: String): List<CertificateSigningRequest> {
        TODO("Not yet implemented")
    }

    override fun submit(body: String): CertificateSigningRequest {
        TODO("Not yet implemented")
    }
}