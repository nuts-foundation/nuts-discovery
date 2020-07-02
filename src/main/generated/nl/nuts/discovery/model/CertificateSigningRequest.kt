package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param subject DN of request
 * @param pem the pem-encoded CSR
 * @param submittedAt Date at which the request was submitted
 */
data class CertificateSigningRequest (

        @get:NotNull 
        @JsonProperty("subject") val subject: String,

        @get:NotNull 
        @JsonProperty("pem") val pem: String,

        @get:NotNull 
        @JsonProperty("submittedAt") val submittedAt: String
) {

}

