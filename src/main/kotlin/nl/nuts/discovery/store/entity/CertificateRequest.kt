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

package nl.nuts.discovery.store.entity

import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.CordaOID
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.CertRole
import net.corda.nodeapi.internal.crypto.CertificateType
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * Entity to store PKCS10CertificationRequest in DB
 */
@Entity
class CertificateRequest {

    companion object {
        /**
         * Create entity from PKCS10CertificationRequest, stores .encoded as bytes
         */
        fun fromPKCS10(request: PKCS10CertificationRequest): CertificateRequest {
            return CertificateRequest().apply {
                name = CordaX500Name.parse(request.subject.toString()).toString() // this puts stuff in right order
                submittedAt = LocalDateTime.now()
                pkcs10Csr = request.encoded
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    /**
     * CN of certificate, like X500Name
     */
    var name: String? = null

    /**
     * encoded bytes of CSR (confirms to DER)
     */
    @Column(name = "pkcs10_csr")
    var pkcs10Csr: ByteArray? = null

    @Column(name = "submitted_at")
    var submittedAt: LocalDateTime? = null

    /**
     * Create PKCS10CertificationRequest from entity, uses raw bytes to create object
     */
    fun toPKCS10(): PKCS10CertificationRequest {
        return PKCS10CertificationRequest(pkcs10Csr)
    }
}