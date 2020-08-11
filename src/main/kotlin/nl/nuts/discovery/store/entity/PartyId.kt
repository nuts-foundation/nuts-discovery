package nl.nuts.discovery.store.entity

import org.bouncycastle.asn1.x509.OtherName
import javax.persistence.AttributeConverter
import javax.persistence.Convert


data class PartyId(val oid: String, val value: String) {

    companion object {
        fun parse(fullyQualified: String): PartyId {
            val parts = fullyQualified.split(":")
            if (parts.size != 4) {
                throw IllegalArgumentException("Invalid number of parts in OID URN: $fullyQualified")
            }
            if (parts[0] != "urn" || parts[1] != "oid") {
                throw IllegalArgumentException("Invalid OID URN: $fullyQualified")
            }
            return PartyId(parts[2], parts[3])
        }

        fun fromOtherName(otherName: OtherName): PartyId {
            return PartyId(otherName.typeID.id, otherName.value.toString())
        }
    }

    override fun toString(): String {
        return "urn:oid:${this.oid}:${this.value}"
    }
}

@Convert
class PartyIdConverter : AttributeConverter<PartyId, String> {

    override fun convertToDatabaseColumn(attribute: PartyId?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): PartyId? {
        return dbData?.let { PartyId.parse(it) }
    }
}