package nl.nuts.discovery.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import nl.nuts.discovery.model.Metadata
import nl.nuts.discovery.model.PartyAttachmentSignature
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 
 * @param metadata 
 * @param cipherText Base64 encoded cipher_text.bin (fhir)
 * @param attachmentHash SHA256 of attachment (metadata + cipherText)
 * @param signatures 
 */
data class ConsentRecord (

        @JsonProperty("metadata") val metadata: Metadata? = null,

        @JsonProperty("cipherText") val cipherText: String? = null,

        @JsonProperty("attachmentHash") val attachmentHash: String? = null,

        @JsonProperty("signatures") val signatures: List<PartyAttachmentSignature>? = null
) {

}

