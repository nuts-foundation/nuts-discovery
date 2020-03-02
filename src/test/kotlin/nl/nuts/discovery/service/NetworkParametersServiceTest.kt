package nl.nuts.discovery.service

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.store.NetworkParametersRepository
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.entity.NetworkParameters
import nl.nuts.discovery.store.entity.Node
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

fun NetworkParametersService.clear() {
    networkParametersRepository.deleteAll()
    nodeRepository.deleteAll()
    initialParams()
}

@RunWith(SpringRunner::class)
@SpringBootTest
class NetworkParametersServiceTest {

    @Autowired
    lateinit var networkParametersService: NetworkParametersService

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var nodeRepository: NodeRepository

    @Before
    fun before() {
        networkParametersService.clear()
    }

    @After
    fun teardown() {
        networkParametersService.clear()
    }

    @Test
    fun `it has the correct minimum platform version`() {
        val parameters = networkParametersService.cordaNetworkParameters(networkParameters())
        assertEquals(4, parameters.minimumPlatformVersion)
    }

    @Test
    fun `when not set, it has no notaries`() {
        val parameters = networkParametersService.cordaNetworkParameters(networkParameters())
        assertEquals(0, parameters.notaries.size)
    }

    @Test
    fun `update network parameters`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL,CN=notary")
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(certificateAndKeyService, subject)
        val node = nodeRepository.save(Node.fromNodeInfo(signedNodeInfo))

        val parameters = networkParametersService.updateNetworkParams(node)
        assertEquals(1, parameters.notaries.size)
        assertEquals(subject, CordaX500Name.parse(parameters.notaries.first().name!!))
    }

    private fun networkParameters(): NetworkParameters {
        return NetworkParameters().apply {
            modifiedTime = LocalDateTime.now()
            id = 1
        }
    }
}