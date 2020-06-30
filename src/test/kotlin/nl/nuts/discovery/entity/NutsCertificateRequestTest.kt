/*
 *     Nuts discovery service
 *     Copyright (C) 2020 Nuts community
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package nl.nuts.discovery.entity

import nl.nuts.discovery.TestUtils.Companion.loadTestCSR
import nl.nuts.discovery.store.entity.NutsCertificateRequest
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class NutsCertificateRequestTest {

    @Test
    fun `parsing correct csr`() {
        val pem = loadTestCSR("test.csr")
        val req = NutsCertificateRequest.parsePEM(pem)

        assertNotNull(req)
    }

    @Test
    fun `extracting oid from correct csr`() {
        val pem = loadTestCSR("test.csr")
        val req = NutsCertificateRequest.parsePEM(pem)
        val oid = NutsCertificateRequest.extractOID(req)

        assertNotNull(oid)
        assertEquals("urn:oid:kvk", oid)
    }

    @Test
    fun `converting correct csr`() {
        val pem = loadTestCSR("test.csr")
        val req = NutsCertificateRequest.fromPEM(pem)

        assertNotNull(req)

        assertEquals("CN=test, O=test, L=town, C=NL", req.name)
        assertEquals("urn:oid:kvk", req.oid)
        assertEquals(pem, req.pem)
    }

    @Test
    fun `converting csr with missing oid`() {
        val pem = loadTestCSR("missing_oid.csr")

        assertFailsWith(IllegalArgumentException::class) {
            NutsCertificateRequest.fromPEM(pem)
        }
    }

    @Test
    fun `converting to PKCS10`() {
        val pem = loadTestCSR("test.csr")
        val req = NutsCertificateRequest.fromPEM(pem).toPKCS10()

        assertNotNull(req)
    }
}