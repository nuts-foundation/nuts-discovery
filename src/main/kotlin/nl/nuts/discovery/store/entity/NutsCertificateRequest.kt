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

import nl.nuts.discovery.DiscoveryException
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import javax.persistence.*


/**
 * Entity to store PEM encoded CSR in DB
 */
@Entity
class NutsCertificateRequest {

    companion object {
        const val NUTS_VENDOR_OID = "1.3.6.1.4.1.54851.4"
        private val SUPPORTED_OTHER_NAMES = listOf(ASN1ObjectIdentifier(NUTS_VENDOR_OID))

        /**
         * Create entity from PKCS10CertificationRequest, stores .encoded as bytes
         * Raises on missing oid or invalid CSR signature
         */
        fun fromPEM(pem: String): NutsCertificateRequest {
            val csr = pemToPKCS10(pem)
            val req = NutsCertificateRequest().apply {
                name = csr.subject.toString()
                this.pem = pem
                submittedAt = LocalDateTime.now()
                oid = getSupportedOtherNames(csr)
                        .filter { it.typeID.id == NUTS_VENDOR_OID }
                        .map { PartyId.fromOtherName(it) }
                        .firstOrNull()
                        ?: throw DiscoveryException("Missing Vendor ID SAN in CSR")
            }

            // check for CSR validity
            val prov = JcaContentVerifierProviderBuilder().build(csr.subjectPublicKeyInfo)
            if (!csr.isSignatureValid(prov)) {
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
         * Find the Party ID in the subjectAltName.otherName extension. Exactly one is expected.
         *
         * throws DiscoveryException on missing oid
         */
        fun getSupportedOtherNames(csr: PKCS10CertificationRequest): List<OtherName> {
            return csr.attributes
                    .filter { it.attrType == PKCSObjectIdentifiers.pkcs_9_at_extensionRequest }
                    .mapNotNull {
                        val extensions = Extensions.getInstance(it.attrValues.getObjectAt(0))
                        val gns = GeneralNames.fromExtensions(extensions, Extension.subjectAlternativeName)
                        getOtherNamesFromSAN(gns)
                    }
                    .flatten()
                    .filter { SUPPORTED_OTHER_NAMES.contains(it.typeID) }
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
    @Convert(converter = PartyIdConverter::class)
    var oid: PartyId? = null

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

fun getOtherNamesFromSAN(gns: GeneralNames): List<OtherName> {
    return gns.names
            .filter { it.tagNo == GeneralName.otherName }
            .map { OtherName.getInstance(DERTaggedObject.getInstance(it.toASN1Primitive()).`object`) }
}