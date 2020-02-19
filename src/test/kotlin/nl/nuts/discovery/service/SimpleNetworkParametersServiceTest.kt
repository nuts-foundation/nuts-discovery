package nl.nuts.discovery.service

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.entity.Node
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class SimpleNetworkParametersServiceTest {

    @Autowired
    lateinit var networkParametersService: NetworkParametersService

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @MockBean
    lateinit var nodeRepository: NodeRepository

    @Test
    fun `it has the correct minimum platform version`() {
        val parameters = this.networkParametersService.networkParameters(null)
        assertEquals(4, parameters.minimumPlatformVersion)
    }

    @Test
    fun `when not set, it has no notaries`() {
        val parameters = this.networkParametersService.networkParameters(null)
        assertEquals(0, parameters.notaries.size)
    }

    @Test
    fun `when set, it returns the notary`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(certificateAndKeyService, subject)
        Mockito.`when`(nodeRepository.findByNameLike("notary")).thenReturn(Node.fromNodeInfo(signedNodeInfo))
        val parameters = this.networkParametersService.networkParameters(null)
        assertEquals(1, parameters.notaries.size)
        assertEquals(subject, parameters.notaries.first().identity.name)
    }
}