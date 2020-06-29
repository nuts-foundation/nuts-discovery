package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param alg 
 * @param iv 
 */
data class SymmetricKey (

        @get:NotNull 
        @JsonProperty("alg") val alg: String,

        @get:NotNull 
        @JsonProperty("iv") val iv: String
) {

}

