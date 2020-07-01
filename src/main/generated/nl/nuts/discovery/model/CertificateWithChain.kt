package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param certificate PEM encoded certificate
 * @param chain PEM encoded list of certificates, the first being the intermediate and the last the root
 */
data class CertificateWithChain (

        @get:NotNull 
        @JsonProperty("certificate") val certificate: String,

        @get:NotNull 
        @JsonProperty("chain") val chain: String
) {

}

