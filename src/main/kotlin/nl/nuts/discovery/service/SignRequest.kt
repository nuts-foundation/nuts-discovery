package nl.nuts.discovery.service

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.CordaX500Name
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.X509Utilities
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest
import java.security.cert.X509Certificate
import java.time.LocalDateTime

data class SignRequest(@JsonIgnore val request: PKCS10CertificationRequest) {
    var submissionTime: LocalDateTime = LocalDateTime.now()

    @JsonIgnore
    var certificate: X509Certificate? = null

    @JsonProperty
    fun name(): String {
        return this.request.subject.toString()
    }

    @JsonProperty
    fun notary(): Boolean {
        val cordaExtension = this.request.getAttributes(ASN1ObjectIdentifier("1.3.6.1.4.1.50530.1.1"))!!.first()
        return cordaExtension!!.attrValues.contains(ASN1Integer(3))
    }

    @JsonProperty
    fun approved(): Boolean {
        return certificate != null
    }

}
