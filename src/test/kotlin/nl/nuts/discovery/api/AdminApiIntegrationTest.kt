package nl.nuts.discovery.api

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.NetworkParametersService
import nl.nuts.discovery.service.NodeInfo
import nl.nuts.discovery.store.CertificateRepository
import nl.nuts.discovery.store.CertificateRequestRepository
import nl.nuts.discovery.store.NetworkParametersRepository
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.entity.CertificateRequest
import nl.nuts.discovery.store.entity.Node
import org.json.JSONArray
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun AdminApi.clear() {
    networkParametersRepository.deleteAll()
    certificateRequestRepository.deleteAll()
    certificateRepository.deleteAll()
    nodeRepository.deleteAll()
}

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminApiIntegrationTest {
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var service: CertificateAndKeyService

    @Autowired
    lateinit var certificateRepository: CertificateRepository

    @Autowired
    lateinit var certificateRequestRepository: CertificateRequestRepository

    @Autowired
    lateinit var networkParametersService: NetworkParametersService

    @Autowired
    lateinit var nodeRepository: NodeRepository

    @Autowired
    lateinit var adminApi: AdminApi

    @Before
    fun setup() {
        adminApi.clear()
    }

    @After
    fun teardown() {
        adminApi.clear()
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
        val signRequest = certificateRequestRepository.save(CertificateRequest.fromPKCS10(req))
        service.signCertificate(signRequest)

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
        certificateRequestRepository.save(CertificateRequest.fromPKCS10(req))

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
    fun `approve signs a certificate`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        certificateRequestRepository.save(CertificateRequest.fromPKCS10(req))

        val entity = HttpEntity("", HttpHeaders())

        val response = testRestTemplate.exchange("/admin/certificates/signrequests/${subject}/approve", HttpMethod.PUT, entity, String::class.java)
        assertEquals(200, response.statusCodeValue)

        assertNotNull(certificateRepository.findByName(subject.toString()))
    }

    @Test
    fun `without nodes, GET network-map returns an empty json list`() {
        val nodesRequest = testRestTemplate.getForEntity("/admin/network-map", String::class.java)
        assertEquals(200, nodesRequest.statusCodeValue)
        assertEquals("[]", nodesRequest.body)
    }

    @Test
    fun `network map returns the notary`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL,CN=notary")
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(service, subject)
        networkParametersService.updateNetworkParams(Node.fromNodeInfo(signedNodeInfo))

        val networkMapRequest = testRestTemplate.getForEntity("/admin/network-parameters", String::class.java)
        assertEquals(200, networkMapRequest.statusCodeValue)
        assertTrue(networkMapRequest.body!!.contains("\"notaries\":[{"))
    }

    @Test
    fun `list nodes returns all nodes`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL,CN=notary")
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(service, subject)
        nodeRepository.save(Node.fromNodeInfo(signedNodeInfo))

        val networkMapRequest = testRestTemplate.getForEntity<List<NodeInfo>>("/admin/network-map")
        assertEquals(200, networkMapRequest.statusCodeValue)
        assertEquals(1, networkMapRequest.body?.size)
    }
}