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

package nl.nuts.discovery.store.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Entity to store salt for serial generation.
 * Serials for signed x509 certificates will be generated using SHA256(salt + number_of_signed_certs)
 * The salt is stored in the DB so it's linked to the signed certificates.
 */
@Entity
@Table(name = "ca_serial")
class CASerial {
    /** DN of CA */
    @Id
    var subject: String? = null

    /** salt used in serial generation */
    var salt: String? = null
}