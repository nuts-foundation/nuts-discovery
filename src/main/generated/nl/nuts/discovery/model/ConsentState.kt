package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import nl.nuts.discovery.model.ConsentId
import nl.nuts.discovery.model.ConsentRecord
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param consentId 
 * @param consentRecords 
 */
data class ConsentState (

        @get:NotNull 
        @JsonProperty("consentId") val consentId: ConsentId,

        @get:NotNull 
        @JsonProperty("consentRecords") val consentRecords: List<ConsentRecord>
) {

}

