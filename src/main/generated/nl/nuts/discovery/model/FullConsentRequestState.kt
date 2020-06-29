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
 * @param legalEntities 
 * @param initiatingLegalEntity Generic identifier used for representing BSN, agbcode, etc. It's always constructed as an URN followed by a colon (:) and then the identifying value of the given URN 
 * @param initiatingNode The X500 name of the node that initiated the transaction (read-only)
 * @param createdAt the date-time when the request was made
 * @param updatedAt the date-time of the latest recorded change in state (read-only)
 * @param comment user generated comment (usually a closing reason)
 */
data class FullConsentRequestState (

        @get:NotNull 
        @JsonProperty("consentId") val consentId: ConsentId,

        @get:NotNull 
        @JsonProperty("consentRecords") val consentRecords: List<ConsentRecord>,

        @get:NotNull 
        @JsonProperty("legalEntities") val legalEntities: List<String>,

        @get:NotNull 
        @JsonProperty("initiatingLegalEntity") val initiatingLegalEntity: String,

        @JsonProperty("initiatingNode") val initiatingNode: String? = null,

        @JsonProperty("createdAt") val createdAt: java.time.OffsetDateTime? = null,

        @JsonProperty("updatedAt") val updatedAt: java.time.OffsetDateTime? = null,

        @JsonProperty("comment") val comment: String? = null
) {

}

