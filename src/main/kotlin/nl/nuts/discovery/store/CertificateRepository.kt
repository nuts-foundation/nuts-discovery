/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2019 Nuts community
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

import nl.nuts.discovery.store.entity.Certificate
import nl.nuts.discovery.store.entity.PartyId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * DB access to certificate
 */
@Repository
interface CertificateRepository: CrudRepository<Certificate, Long> {
    /**
     * Find the certificate with the specified name as CN
     *
     * @return certificate
     */
    fun findByName(name: String) : Certificate?

    /**
     * Find certificates with the specified oid in the SAN
     */
    fun findByOid(oid: PartyId) : List<Certificate>

    /**
     * Find the number of certificates signed by a specific CA
     */
    fun countByCa(caSubject: String): Int
}
