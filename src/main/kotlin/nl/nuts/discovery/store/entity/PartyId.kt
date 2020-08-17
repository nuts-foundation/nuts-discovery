package nl.nuts.discovery.store.entity

import org.bouncycastle.asn1.x509.OtherName
import javax.persistence.AttributeConverter
import javax.persistence.Convert


/**
 * PartyId describes an ID identifying a party on the Nuts Network. A party is identified using an OID indicating the
 * type/role of the party and a value uniquely identifying that party within that OID.
 *
 * Example: urn:oid:1.2.3:foobar ("1.2.3" being the OID and "foobar" the value).
 *
 * An OID is always string consisting of dot-separated numbers and should be well-known by the processing parties.
 * The value part should be alphanumeric.
 */
data class PartyId(val oid: String, val value: String) {

    companion object {
        /**
         * Parses a fully qualified PartyId in the form of urn:oid:<OID>:<value>
         */
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

        /**
         * fromOtherName converts a X.509 OtherName to a PartyId.
         */
        fun fromOtherName(otherName: OtherName): PartyId {
            return PartyId(otherName.typeID.id, otherName.value.toString())
        }
    }

    /**
     * Returns this PartyId to its fully qualified form.
     */
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