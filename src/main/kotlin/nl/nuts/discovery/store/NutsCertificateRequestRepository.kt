/*
 *     Nuts discovery service
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

package nl.nuts.discovery.store

import nl.nuts.discovery.store.entity.NutsCertificateRequest
import nl.nuts.discovery.store.entity.PartyId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * DB access to nuts_certificate_request
 */
@Repository
interface NutsCertificateRequestRepository: CrudRepository<NutsCertificateRequest, Long> {
    /**
     * Find the CSR with the specified oid
     *
     * @return CSR
     */
    fun findByOid(oid: PartyId) : List<NutsCertificateRequest>
}
