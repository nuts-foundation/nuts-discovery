package nl.nuts.discovery.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.CordaOID
import net.corda.core.identity.CordaX500Name
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.pkcs_9_at_emailAddress
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.cert.X509Certificate
import java.time.LocalDateTime

data class SignRequest(@JsonIgnore val request: PKCS10CertificationRequest) {
    var submissionTime: LocalDateTime = LocalDateTime.now()

    @JsonIgnore
    var certificate: X509Certificate? = null

    @JsonProperty
    fun legalName(): CordaX500Name {
        return CordaX500Name.parse(this.request.subject.toString())
    }

    @JsonProperty
    fun notary(): Boolean {
        val cordaExtension = this.request.getAttributes(ASN1ObjectIdentifier(CordaOID.X509_EXTENSION_CORDA_ROLE))!!.first()
        return cordaExtension!!.attrValues.contains(ASN1Integer(3))
    }

    @JsonProperty
    fun email(): String {
        val emailAttr = this.request.getAttributes(BCStyle.EmailAddress)!!.first()
        val emailASN1String = emailAttr!!.attrValues.getObjectAt(0) as ASN1String
        return emailASN1String.string
    }

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

    @JsonProperty
    fun approved(): Boolean {
        return certificate != null
    }

}
