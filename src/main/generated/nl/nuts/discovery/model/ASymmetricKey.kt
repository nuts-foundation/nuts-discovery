package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param legalEntity Generic identifier used for representing BSN, agbcode, etc. It's always constructed as an URN followed by a colon (:) and then the identifying value of the given URN 
 * @param alg 
 * @param cipherText base64 encoded
 */
data class ASymmetricKey (

        @get:NotNull 
        @JsonProperty("legalEntity") val legalEntity: String,

        @JsonProperty("alg") val alg: String? = null,

        @JsonProperty("cipherText") val cipherText: String? = null
) {

}

