package nl.nuts.discovery.api

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiIntegrationTest {
    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Autowired
    lateinit var service : CertificateAndKeyService

    @After
    fun clearNodes(){
        service.clearAll()
    }

    @Test
    fun `without approved nodes, GET nodes returns an empty json array`() {
        val approvedNodesResponse = testRestTemplate.getForEntity("/admin/nodes", String::class.java)
        assertEquals(HttpStatus.OK, approvedNodesResponse.statusCode)
        assertEquals("[]", approvedNodesResponse.body)
    }

    @Test
    fun `with an approved node, GET nodes returns the node`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        service.submitSigningRequest(req)
        service.signCertificate(subject)

        val approvedNodesResponse = testRestTemplate.getForEntity("/admin/nodes", String::class.java)
        assertEquals(HttpStatus.OK, approvedNodesResponse.statusCode)
        assertEquals("[{\"name\":\"O=Org, L=Gr, C=NL\",\"approved\":true}]", approvedNodesResponse.body)
    }

    @Test
    fun `without pending nodes, GET nodes-pending returns an empty json array`() {
        val approvedNodesResponse = testRestTemplate.getForEntity("/admin/nodes/pending", String::class.java)
        assertEquals(HttpStatus.OK, approvedNodesResponse.statusCode)
        assertEquals("[]", approvedNodesResponse.body)
    }

    @Test
    fun `with a pending nodes, GET nodes-pending returns an empty json array`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        service.submitSigningRequest(req)

        val approvedNodesResponse = testRestTemplate.getForEntity("/admin/nodes/pending", String::class.java)
        assertEquals(HttpStatus.OK, approvedNodesResponse.statusCode)
        assertEquals("[{\"name\":\"O=Org, L=Gr, C=NL\",\"approved\":false}]", approvedNodesResponse.body)
    }

}