package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param validFrom 
 * @param validTo 
 */
data class Period (

        @get:NotNull 
        @JsonProperty("validFrom") val validFrom: java.time.OffsetDateTime,

        @JsonProperty("validTo") val validTo: java.time.OffsetDateTime? = null
) {

}

