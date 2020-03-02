package nl.nuts.discovery.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.toAttributesMap
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.cert.X509Certificate

/**
 * SignRequest wraps a X509Certificate and provides extra properties to serialize it as json.
 */
data class Certificate(@JsonIgnore var certificate: X509Certificate) {
    /**
     * Returns the CordaX500Name from the subject
     */
    @JsonProperty
    fun legalName(): CordaX500Name {
        return CordaX500Name.parse(certificate.subjectDN.name)
    }

    /**
     * Returns whether or not this node is a notary
     */
    @JsonProperty
    fun notary(): Boolean {
        return certificate.subjectDN.name.contains("notary")
    }

    /**
     * returns the email from the certificate request
     */
    @JsonProperty
    fun email(): String? {
        return certificate.issuerX500Principal.toAttributesMap(
            setOf(BCStyle.EmailAddress, BCStyle.CN, BCStyle.C, BCStyle.L, BCStyle.O)
        )[BCStyle.EmailAddress]
    }

    /**
     * The public key of the Certificate Request
     */
    @JsonProperty
    fun publicKey(): String {
        val str = StringWriter()
        val pemWriter = PemWriter(str)
        val pemObject = PemObject("CERTIFICATE REQUEST", certificate.encoded)
        pemWriter.writeObject(pemObject)
        pemWriter.close()
        str.close()
        return str.toString()
    }

    /**
     * Whether or not this request is approved.
     */
    @JsonProperty
    fun approved(): Boolean {
        return true
    }

}
