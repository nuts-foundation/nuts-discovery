package nl.nuts.discovery.api

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ManagementApiTest {
    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Test
    fun `submitting with missing headers returns 400`() {
        val response = testRestTemplate.getForEntity("/manage/pending", String::class.java)

        assertEquals(200, response.statusCodeValue)
    }

}