package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param publicKey as described by https://tools.ietf.org/html/rfc7517. Modelled as object so libraries can parse the tokens themselves.
 * @param &#x60;data&#x60; base64 encoded bytes
 */
data class SignatureWithKey (

        @get:NotNull 
        @JsonProperty("publicKey") val publicKey: Map<kotlin.String, Any>,

        @get:NotNull 
        @JsonProperty("data") val `data`: String
) {

}

