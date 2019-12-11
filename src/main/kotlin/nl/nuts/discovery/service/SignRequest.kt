package nl.nuts.discovery.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.CordaOID
import net.corda.core.identity.CordaX500Name
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.cert.X509Certificate
import java.time.LocalDateTime

/**
 * SignReuest wraps a PKCS10CertificationRequest and provides extra properties to serialize it as json.
 */
data class SignRequest(@JsonIgnore val request: PKCS10CertificationRequest) {
    var submissionTime: LocalDateTime = LocalDateTime.now()

    @JsonIgnore
    var certificate: X509Certificate? = null

    /**
     * Returns the CordaX500Name from the subject
     */
    @JsonProperty
    fun legalName(): CordaX500Name {
        return CordaX500Name.parse(this.request.subject.toString())
    }

    /**
     * Returns whether or not this node is a notary
     */
    @JsonProperty
    fun notary(): Boolean {
        val cordaExtension = this.request.getAttributes(ASN1ObjectIdentifier(CordaOID.X509_EXTENSION_CORDA_ROLE))!!.first()
        return cordaExtension!!.attrValues.contains(ASN1Integer(3))
    }

    /**
     * returns the email from the certificate request
     */
    @JsonProperty
    fun email(): String {
        val emailAttr = this.request.getAttributes(BCStyle.EmailAddress)!!.first()
        val emailASN1String = emailAttr!!.attrValues.getObjectAt(0) as ASN1String
        return emailASN1String.string
    }

    /**
     * The public key of the Certificate Request
     */
    @JsonProperty
    fun publicKey(): String {
        this.request
        val str = StringWriter()
        val pemWriter = PemWriter(str)
        val pemObject = PemObject("CERTIFICATE REQUEST", this.request.encoded)
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
        return certificate != null
    }

}
