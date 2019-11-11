package nl.nuts.discovery.api

import com.natpryce.hamkrest.containsSubstring
import junit.framework.Assert.assertTrue
import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.SignRequest
import nl.nuts.discovery.store.NodeRepository
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiIntegrationTest {
    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Autowired
    lateinit var service : CertificateAndKeyService

    @Autowired
    lateinit var nodeRepo : NodeRepository

    @After
    fun clearNodes(){
        service.clearAll()
        nodeRepo.clearAll()
    }

    @Test
    fun `without approved certificates, GET certificates returns an empty json array`() {
        val approvedNodesResponse = testRestTemplate.getForEntity("/admin/certificates", String::class.java)
        assertEquals(HttpStatus.OK, approvedNodesResponse.statusCode)
        assertEquals("[]", approvedNodesResponse.body)
    }

    @Test
    fun `with an approved certificate, GET certificates returns the certificate`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        service.submitSigningRequest(req)
        service.signCertificate(subject)

        val signedCertificates = testRestTemplate.getForEntity("/admin/certificates", String::class.java)
        assertEquals(200, signedCertificates.statusCodeValue)
        assertTrue(signedCertificates.body!!.contains("O=Org"))
    }

    @Test
    fun `without pending requests, GET certificates-signrequests returns an empty json array`() {
        val approvedNodesResponse = testRestTemplate.getForEntity("/admin/certificates/signrequests", String::class.java)
        assertEquals(HttpStatus.OK, approvedNodesResponse.statusCode)
        assertEquals("[]", approvedNodesResponse.body)
    }

    @Test
    fun `with a pending request, GET signrequests returns the request`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        service.submitSigningRequest(req)

        val signRequests = testRestTemplate.getForEntity("/admin/certificates/signrequests", String::class.java)
        assertEquals(200, signRequests.statusCodeValue)
        assertTrue(signRequests.body!!.contains("O=Org"))
        assertTrue(signRequests.body!!.contains("L=Gr"))
    }

    @Test
    fun `without nodes, GET network-map returns an empty json list`() {
        val nodesRequest = testRestTemplate.getForEntity("/admin/network-map", String::class.java)
        assertEquals(200, nodesRequest.statusCodeValue)
        assertEquals("[]", nodesRequest.body)
    }

}