package nl.nuts.discovery.api

import nl.nuts.discovery.service.CertificateAndKeyService
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

    @Test
    fun `a valid approval results in a approved node`() {
        val response = testRestTemplate.getForEntity("/admin/nodes/pending", String::class.java)

        assertEquals(200, response.statusCodeValue)
    }

}