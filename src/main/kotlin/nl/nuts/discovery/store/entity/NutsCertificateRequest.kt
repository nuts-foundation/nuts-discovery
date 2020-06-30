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

import net.corda.core.identity.CordaX500Name
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


/**
 * Entity to store PEM encoded CSR in DB
 */
@Entity
class NutsCertificateRequest {

    companion object {
        val NUTS_VENDOR_EXTENSION = ASN1ObjectIdentifier("1.3.6.1.4.1.54851.4").intern()

        /**
         * Create entity from PKCS10CertificationRequest, stores .encoded as bytes
         */
        fun fromPEM(pem: String): NutsCertificateRequest {
            return NutsCertificateRequest().apply {
                val csr = parsePEM(pem)

                name = CordaX500Name.parse(csr.subject.toString()).toString() // this puts stuff in right order
                this.pem = pem
                submittedAt = LocalDateTime.now()
                oid = extractOID(csr)
            }
        }

        fun parsePEM(pem: String): PKCS10CertificationRequest {
            ByteArrayInputStream(pem.toByteArray(Charsets.UTF_8)).use {
                val pemParser = PEMParser(BufferedReader(InputStreamReader(it)))
                val parsedObj = pemParser.readObject()
                if (parsedObj is PKCS10CertificationRequest) {
                    return parsedObj
                }

                throw IllegalArgumentException("Couldn't parse PEM as CERTIFICATE REQUEST")
            }
        }

        fun extractOID(csr: PKCS10CertificationRequest): String {
            val certAttributes = csr.attributes
            for (attribute in certAttributes) {
                if (attribute.attrType == NUTS_VENDOR_EXTENSION) {
                    return attribute.attrValues.getObjectAt(0).toString()
                }
            }

            throw IllegalArgumentException("Given CSR does not have a correctly formatted oid in extensions")
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    /**
     * CN of certificate, like X500Name
     */
    var name: String? = null

    /** vendor oid from attributes */
    var oid: String? = null

    /** PEM encoded CSR */
    @Column(name = "pem")
    var pem: String? = null

    @Column(name = "submitted_at")
    var submittedAt: LocalDateTime? = null

    /**
     * Create PKCS10CertificationRequest from entity
     */
    fun toPKCS10(): PKCS10CertificationRequest {
        val reader = StringReader(pem)
        val pemReader = PEMParser(reader)

        return pemReader.readObject() as PKCS10CertificationRequest
    }
}