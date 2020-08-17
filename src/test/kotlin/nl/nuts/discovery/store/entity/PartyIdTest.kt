package nl.nuts.discovery.store.entity

import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class PartyIdTest {

    @Test
    fun testToString() {
        assertEquals("urn:oid:1.2.3:456", PartyId("1.2.3", "456").toString())
    }

    @Test
    fun parse() {
        assertEquals(PartyId("1.2.3", "456"), PartyId.parse("urn:oid:1.2.3:456"))
    }

    @Test
    fun `parse with invalid string`() {
        assertThrows<IllegalArgumentException> { PartyId.parse("adasdsadsad") }
    }

    @Test
    fun `parse with invalid number of parts`() {
        assertThrows<IllegalArgumentException> { PartyId.parse("urn:oid:1:2:3:4") }
    }


    @Test
    fun `parse with non-urn identifier`() {
        assertThrows<IllegalArgumentException> { PartyId.parse("namespace:file:/etc/passwd") }
    }

    @Test
    fun `parse with invalid URN scheme (scheme != oid)`() {
        assertThrows<IllegalArgumentException> { PartyId.parse("urn:file:/etc/passwd") }
    }
}

class PartyIdConverterTest {

    @Test
    fun convertToDatabaseColumn() {
        assertEquals("urn:oid:1.2.3:foobar", PartyIdConverter().convertToDatabaseColumn(PartyId("1.2.3", "foobar")))
    }

    @Test
    fun convertToEntityAttribute() {
        assertEquals(PartyId("1.2.3", "foobar"), PartyIdConverter().convertToEntityAttribute("urn:oid:1.2.3:foobar"))
    }
}