package nl.nuts.discovery.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.security.cert.X509Certificate
import java.time.LocalDateTime

data class SignRequest(var name: String, var approved: Boolean, var submissionTime: LocalDateTime) {
    @JsonIgnore
    var certificate: X509Certificate? = null

    @JsonIgnore
    var request: PKCS10CertificationRequest? = null

    constructor(request: PKCS10CertificationRequest) : this(request.subject.toString(), false, LocalDateTime.now()) {
        this.request = request
    }
}
