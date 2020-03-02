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

import net.corda.core.CordaOID
import net.corda.core.internal.CertRole
import org.bouncycastle.asn1.ASN1Integer
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


/**
 * represents X509 certificate
 */
@Entity
class Certificate {

    companion object {
        /**
         * Create entity from a X509Certificate object, stores .encoded as bytes
         */
        fun fromX509Certificate(certificate: X509Certificate): Certificate {
            return Certificate().apply {
                name = certificate.subjectDN.name
                x509 = certificate.encoded
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
     * encoded bytes of X509 (confirms to DER)
     */
    var x509: ByteArray? = null

    /**
     * create a X509Certificate from the raw bytes (DER encoding)
     */
    fun toX509(): X509Certificate {
        val certFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(x509)) as X509Certificate
    }

    fun notary(): Boolean {
        return CertRole.extract(toX509()) == CertRole.SERVICE_IDENTITY
    }
}