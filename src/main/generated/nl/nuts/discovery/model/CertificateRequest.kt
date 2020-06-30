package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param name CN of request
 * @param oid oid of vendor, can be used to query requests
 * @param pem the pem-encoded CSR
 * @param submittedAt Date at which the request was submitted
 */
data class CertificateRequest (

        @get:NotNull 
        @JsonProperty("name") val name: String,

        @get:NotNull 
        @JsonProperty("oid") val oid: String,

        @get:NotNull 
        @JsonProperty("pem") val pem: String,

        @get:NotNull 
        @JsonProperty("submittedAt") val submittedAt: String
) {

}

