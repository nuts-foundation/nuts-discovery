/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2019 Nuts community
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

package nl.nuts.discovery.api

import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.model.CertificateRequest
import nl.nuts.discovery.service.NutsDiscoveryProperties
import nl.nuts.discovery.store.NutsCertificateRequestRepository
import nl.nuts.discovery.store.entity.NutsCertificateRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class NutsCertificateApiIntegrationTest {
    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @Before
    fun clearNodes(){
        nutsCertificateRequestRepository.deleteAll()
        nutsDiscoveryProperties.autoAck = false
    }

    @Test
    fun`valid submit request returns 200`() {
        val req = TestUtils.loadTestCSR("test.csr")
        val entity = HttpEntity(req)

        val response = testRestTemplate.exchange("/api/csr", HttpMethod.POST, entity, CertificateRequest::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("urn:oid:kvk", response.body?.oid)

        val ldt = response.body?.submittedAt
        // no explosions
        LocalDateTime.parse(ldt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @Test
    fun`invalid submit request returns 400`() {
        val req = TestUtils.loadTestCSR("missing_oid.csr")
        val entity = HttpEntity(req)

        val response = testRestTemplate.exchange("/api/csr", HttpMethod.POST, entity, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

    @Test
    fun `list returns list of requests`() {
        val pem = TestUtils.loadTestCSR("test.csr")
        nutsCertificateRequestRepository.save(NutsCertificateRequest.fromPEM(pem))

        val response = testRestTemplate.exchange("/api/csr/urn:oid:kvk", HttpMethod.GET, null, typeReference<List<CertificateRequest>>())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
    }
}