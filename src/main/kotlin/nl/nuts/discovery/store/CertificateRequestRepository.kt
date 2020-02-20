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

import nl.nuts.discovery.store.entity.CertificateRequest
import nl.nuts.discovery.store.entity.Node
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CertificateRequestRepository: CrudRepository<CertificateRequest, Long> {
    /**
     * Find the CSR with the specified name as CN
     *
     * @return CSR
     */
    fun findByName(name: String) : CertificateRequest?
}
