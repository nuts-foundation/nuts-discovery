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

import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.OtherName
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.persistence.*


/**
 * represents X509 certificate
 */
@Entity
class Certificate {

    companion object {
        /**
         * Create entity from a X509Certificate object, stores .encoded as bytes
         */
        fun fromX509Certificate(certificate: X509Certificate, caSubject: String, chain: String): Certificate {
            return Certificate().apply {
                name = certificate.subjectDN.name
                x509 = certificate.encoded
                oid = getSinglePartyID(certificate)
                ca = caSubject
                this.chain = chain
            }
        }

        private fun getSinglePartyID(certificate: X509Certificate): PartyId? {
            return certificate.subjectAlternativeNames
                    // Take SANs of type OtherName
                    .filter { it[0] == 0 }
                    .map { it[1] }
                    .map { OtherName.getInstance(DERTaggedObject.getInstance(it).`object`) }
                    // We expect exactly 1 OtherName SAN
                    .let {
                        when {
                            it.isEmpty() -> null
                            it.size == 1 -> PartyId.fromOtherName(it[0])
                            else -> throw IllegalArgumentException("Multiple OtherName SANs in certificate")
                        }
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
     * Nuts org/vendor identifier
     */
    @Convert(converter = PartyIdConverter::class)
    var oid: PartyId? = null

    /**
     * encoded bytes of X509 (confirms to DER)
     */
    var x509: ByteArray? = null

    /**
     * any intermediate CA and the root, stored for historical purposes
     */
    var chain: String? = null

    /**
     * the signing CA's subject
     */
    var ca: String? = null

    /**
     * create a X509Certificate from the raw bytes (DER encoding)
     */
    fun toX509(): X509Certificate {
        val certFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(x509)) as X509Certificate
    }

    /**
     * Return this certificate in PEM format
     */
    fun toPem(): String {
        val str = StringWriter()
        val pemWriter = PemWriter(str)
        val pemObject = PemObject("CERTIFICATE", x509)
        pemWriter.writeObject(pemObject)
        pemWriter.close()
        str.close()
        return str.toString()
    }
}