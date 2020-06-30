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
import nl.nuts.discovery.service.NutsDiscoveryProperties
import nl.nuts.discovery.store.NutsCertificateRequestRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

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
    fun`valid request returns 200`() {
        val req = TestUtils.loadTestCSR("test.csr")
        val entity = HttpEntity(req)

        val response = testRestTemplate.exchange("/api/csr", HttpMethod.POST, entity, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun`invalid request returns 400`() {
        val req = TestUtils.loadTestCSR("missing_oid.csr")
        val entity = HttpEntity(req)

        val response = testRestTemplate.exchange("/api/csr", HttpMethod.POST, entity, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}