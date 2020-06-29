package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import nl.nuts.discovery.model.ASymmetricKey
import nl.nuts.discovery.model.Domain
import nl.nuts.discovery.model.Period
import nl.nuts.discovery.model.SymmetricKey
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param domain 
 * @param secureKey 
 * @param organisationSecureKeys 
 * @param period 
 * @param previousAttachmentHash SHA256 of cipherText bytes
 * @param consentRecordHash Hash of the unencrypted consent FHIR resource. Can be used for uniqueness.
 */
data class Metadata (

        @get:NotNull 
        @JsonProperty("domain") val domain: List<Domain>,

        @get:NotNull 
        @JsonProperty("secureKey") val secureKey: SymmetricKey,

        @get:NotNull 
        @JsonProperty("organisationSecureKeys") val organisationSecureKeys: List<ASymmetricKey>,

        @get:NotNull 
        @JsonProperty("period") val period: Period,

        @get:NotNull 
        @JsonProperty("consentRecordHash") val consentRecordHash: String,

        @JsonProperty("previousAttachmentHash") val previousAttachmentHash: String? = null
) {

}

