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
import nl.nuts.discovery.DiscoveryException
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.pkcs.Attribute
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
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
        val NUTS_VENDOR_OID = "1.3.6.1.4.1.54851.4"
        val NUTS_VENDOR_EXTENSION: ASN1ObjectIdentifier = ASN1ObjectIdentifier(NUTS_VENDOR_OID).intern()

        /**
         * Create entity from PKCS10CertificationRequest, stores .encoded as bytes
         * Raises on missing oid or invalid CSR signature
         */
        fun fromPEM(pem: String): NutsCertificateRequest {
            val csr = pemToPKCS10(pem)
            val req = NutsCertificateRequest().apply {
                name = CordaX500Name.parse(csr.subject.toString()).toString() // this puts stuff in right order
                this.pem = pem
                submittedAt = LocalDateTime.now()
                oid = "urn:oid:${extractOID(csr)}"
            }

            // check for CSR validity
            val prov = JcaContentVerifierProviderBuilder().build(csr.subjectPublicKeyInfo)
            if(!csr.isSignatureValid(prov)) {
                throw DiscoveryException("Invalid signature")
            }

            return req
        }

        /**
         * Parse a pem-encoded CSR
         */
        fun pemToPKCS10(pem: String): PKCS10CertificationRequest {
            ByteArrayInputStream(pem.toByteArray(Charsets.UTF_8)).use {
                val pemParser = PEMParser(BufferedReader(InputStreamReader(it)))
                val parsedObj = pemParser.readObject()
                if (parsedObj is PKCS10CertificationRequest) {
                    return parsedObj
                }

                throw DiscoveryException("Couldn't parse PEM as CERTIFICATE REQUEST")
            }
        }

        /**
         * Find the OID in the subjectAltName.otherName extension
         *
         * throws DiscoveryException on missing oid
         */
        fun extractOID(csr: PKCS10CertificationRequest): String {
            var vendor: String? = null
            val certAttributes = csr.attributes
            for (attribute in certAttributes) {
                if (attribute.attrType.equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    vendor = nutsVendorFromExtReq(attribute)
                    if (vendor != null) {
                        break
                    }
                }
            }

            if (vendor == null) {
                throw DiscoveryException("Given CSR does not have a correctly formatted oid in extensions")
            }

            return "$NUTS_VENDOR_OID:$vendor"
        }

        private fun nutsVendorFromExtReq(attribute: Attribute) : String? {
            var vendor: String? = null

            val extensions = Extensions.getInstance(attribute.attrValues.getObjectAt(0))
            val gns = GeneralNames.fromExtensions(extensions, Extension.subjectAlternativeName)
            val names = gns.names
            for (san in names) {
                if (san.tagNo == GeneralName.otherName && san.name is DLSequence) {
                    val oid = (san.name as DLSequence).getObjectAt(0)
                    if (oid == NUTS_VENDOR_EXTENSION) {
                        val taggedObject = (san.name as DLSequence).getObjectAt(1) as DERTaggedObject
                        val value = taggedObject.`object` as DERUTF8String
                        vendor = value.toString()
                        break
                    }
                }
            }

            return vendor
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