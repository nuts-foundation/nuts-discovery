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

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.NutsDiscoveryProperties
import nl.nuts.discovery.store.CertificateRepository
import nl.nuts.discovery.store.CertificateRequestRepository
import nl.nuts.discovery.store.entity.CertificateRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringRunner
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CertificateApiIntegrationTest {
    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Autowired
    lateinit var certificateRequestRepository: CertificateRequestRepository

    @Autowired
    lateinit var certificateRepository: CertificateRepository

    @Autowired
    lateinit var service: CertificateAndKeyService

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @Before
    fun clearNodes(){
        certificateRequestRepository.deleteAll()
        certificateRepository.deleteAll()
        nutsDiscoveryProperties.autoAck = false
    }

    @Test
    fun `submitting with missing headers returns 400`() {
        val response = testRestTemplate.postForEntity("/doorman/certificate", "", ByteArray::class.java)

        assertEquals(400, response.statusCodeValue)
    }

    @Test
    fun `submitting an invalid CSR returns 400`() {
        val response = testRestTemplate.exchange("/doorman/certificate", HttpMethod.POST, HttpEntity<Any>(headers()), ByteArray::class.java)

        assertEquals(400, response.statusCodeValue)
    }

    @Test
    fun`valid request returns X500Name as result`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)

        val entity = HttpEntity(req.encoded, headers())

        val response = testRestTemplate.exchange("/doorman/certificate", HttpMethod.POST, entity, ByteArray::class.java)

        val bodyBytes = response.body
        assertNotNull(bodyBytes)
        assertEquals(subject, CordaX500Name.parse(String(bodyBytes!!)))
    }

    @Test
    fun`valid request is stored under the right name`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)

        val entity = HttpEntity(req.encoded, headers())

        testRestTemplate.exchange("/doorman/certificate", HttpMethod.POST, entity, ByteArray::class.java)

        val sr = certificateRequestRepository.findByName(subject.toString())
        assertNotNull(sr)
    }

    @Test
    fun`valid request will be pending`() {
        val orgName = "O=Org,L=Gr,C=NL"
        val subject = CordaX500Name.parse(orgName)
        val req = TestUtils.createCertificateRequest(subject)

        val entity = HttpEntity(req.encoded, headers())

        testRestTemplate.exchange("/doorman/certificate", HttpMethod.POST, entity, ByteArray::class.java)
        val response = testRestTemplate.getForEntity("/doorman/certificate/$orgName", ByteArray::class.java)

        assertEquals(204, response.statusCodeValue)
    }

    @Test
    fun`valid request will be signed with autoAck enabled`() {
        nutsDiscoveryProperties.autoAck = true
        val orgName = "O=Org,L=Gr,C=NL"
        val subject = CordaX500Name.parse(orgName)
        val req = TestUtils.createCertificateRequest(subject)

        val entity = HttpEntity(req.encoded, headers())

        testRestTemplate.exchange("/doorman/certificate", HttpMethod.POST, entity, ByteArray::class.java)
        val response = testRestTemplate.getForEntity("/doorman/certificate/$orgName", ByteArray::class.java)

        assertEquals(200, response.statusCodeValue)
    }

    @Test
    fun `an unknown certificate will return a 403`(){
        val response = testRestTemplate.getForEntity("/doorman/certificate/O=Org,L=Gr,C=NL", ByteArray::class.java)
        assertEquals(403, response.statusCodeValue)
        assertNull(response.body)
    }

    @Test
    fun `download an approved certificate, GET certificates returns the certificat`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        val signRequest = certificateRequestRepository.save(CertificateRequest.fromPKCS10(req))
        service.signCertificate(signRequest)

        val response = testRestTemplate.getForEntity("/doorman/certificate/${subject}", ByteArray::class.java)
        assertEquals(200, response.statusCodeValue)

        var found = false
        val zip = ZipInputStream(ByteArrayInputStream(response.body))
        zip.use {
            do {
                var entry: ZipEntry? = zip.nextEntry

                if (entry == null) {
                    break
                }

                if (entry.name.endsWith(".cer")) {
                    found = true
                }
            } while (entry != null)
        }
        assertTrue(found)
    }

    private fun headers() : HttpHeaders {
        val headers = HttpHeaders()
        headers.add("Platform-Version", "3")
        headers.add("Client-Version", "1")

        return headers
    }
}