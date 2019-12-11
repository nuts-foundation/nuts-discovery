package nl.nuts.discovery.service

import net.corda.core.identity.Party
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import nl.nuts.discovery.store.NodeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.security.cert.X509Certificate
import java.time.Instant

/**
 * Simple stateless NetworkParameters Service.
 * It ignores the given hashes and always returns the same version.
 * It can be used in development, small tests and early production networks.
 * In the later prod network, parameters updates will happen and multiple versions of
 * the parameters must be supported.
 */
@Profile(value = arrayOf("dev", "test", "default"))
@Service
class SimpleNetworkParametersService : NetworkParametersService {

    @Autowired
    private lateinit var nodeRepository: NodeRepository

    val minPlatformVersion = 4
    val maxMessageSize = 10 * 1024 * 1024
    val maxTransactionSize = 10 * 1024 * 1024

    override fun networkParameters(versionHash: String?): NetworkParameters {
        val notaries = mutableListOf<X509Certificate>()
        val notary = this.nodeRepository.notary()?.verified()?.legalIdentitiesAndCerts?.first()?.certificate
        if (notary != null) {
            notaries.add(notary)
        }
        return NetworkParameters(
            minPlatformVersion,
            notaries.map { (NotaryInfo(Party(it), false)) },
            maxMessageSize,
            maxTransactionSize,
            Instant.EPOCH,
            1,
            linkedMapOf()
        )
    }
}