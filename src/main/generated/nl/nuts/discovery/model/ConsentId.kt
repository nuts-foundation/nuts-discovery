package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param externalId Unique hexadecimal identifier created based on BSN and private key of care provider.
 * @param UUID Unique identifier assigned by the consent cordapp
 */
data class ConsentId (

        @get:NotNull 
        @JsonProperty("UUID") val UUID: String,

        @JsonProperty("externalId") val externalId: String? = null
) {

}

