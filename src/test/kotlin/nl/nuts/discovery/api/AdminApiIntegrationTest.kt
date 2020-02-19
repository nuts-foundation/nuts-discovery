package nl.nuts.discovery.api

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.SignRequestStore
import nl.nuts.discovery.store.entity.Node
import org.json.JSONArray
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiIntegrationTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var service: CertificateAndKeyService

    @Autowired
    lateinit var signRequestStore: SignRequestStore

    @MockBean
    lateinit var nodeRepo: NodeRepository

    @Before
    fun clearNodes() {
        signRequestStore.clearAll()
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
        val signRequest = signRequestStore.addSigningRequest(req)
        service.signCertificate(req)
        signRequestStore.markAsSigned(signRequest)

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
        signRequestStore.addSigningRequest(req)

        val signRequests = testRestTemplate.getForEntity("/admin/certificates/signrequests", String::class.java)
        val body = signRequests.body
        assertNotNull(body)
        val arr = JSONArray(body)
        val obj = arr.getJSONObject(0)
        assertEquals(obj.getJSONObject("legalName").getString("locality"), "Gr")
        assertEquals("a@b.com", obj.getString("email"))
        assertEquals(200, signRequests.statusCodeValue)
    }

    @Test
    fun `without nodes, GET network-map returns an empty json list`() {
        val nodesRequest = testRestTemplate.getForEntity("/admin/network-map", String::class.java)
        assertEquals(200, nodesRequest.statusCodeValue)
        assertEquals("[]", nodesRequest.body)
    }

    @Test
    fun `network map returns the notary`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(service, subject)
        Mockito.`when`(nodeRepo.findByNameLike("notary")).thenReturn(Node().apply { raw = signedNodeInfo.raw.bytes })

        val networkMapRequest = testRestTemplate.getForEntity("/admin/network-parameters", String::class.java)
        assertEquals(200, networkMapRequest.statusCodeValue)
        assertTrue(networkMapRequest.body!!.contains("\"notaries\":[{"))
    }
}