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

import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import sun.misc.BASE64Encoder
import sun.security.provider.X509Factory
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
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
        fun fromX509Certificate(certificate: X509Certificate, caSubject: String, chain: String): Certificate {
            return Certificate().apply {
                name = certificate.subjectDN.name
                x509 = certificate.encoded
                oid = extractOID(certificate)
                ca = caSubject
                this.chain = chain
            }
        }

        /**
         * Find Nuts identifier in extensions
         */
        fun extractOID(certificate: X509Certificate): String? {
            val sans = JcaX509ExtensionUtils.getSubjectAlternativeNames(certificate)
            sans.forEach {// GeneralNames
                val generalName = it as List<*> // GeneralName
                if (generalName[0] == GeneralName.otherName) {
                    // then this should be a DLSequence
                    val seq = generalName[1] as DLSequence
                    if (seq.getObjectAt(0) == NutsCertificateRequest.NUTS_VENDOR_EXTENSION) {
                        return (seq.getObjectAt(1) as DERUTF8String).toString()
                    }
                }
            }
            return null
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
    var oid: String? = null

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