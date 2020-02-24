package nl.nuts.discovery.service

import net.corda.core.identity.Party
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import nl.nuts.discovery.store.NetworkParametersRepository
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.entity.Node
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.transaction.Transactional

/**
 * Simple stateless NetworkParameters Service.
 * It ignores the given hashes and always returns the same version.
 * It can be used in development, small tests and early production networks.
 * In the later prod network, parameters updates will happen and multiple versions of
 * the parameters must be supported.
 */
@Service
class NetworkParametersService {

    @Autowired
    lateinit var networkParametersRepository: NetworkParametersRepository

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var nodeRepository: NodeRepository

    /**
     * todo: Hardcoded values, if these change, stored hashes are no longer valid!
     */
    val minPlatformVersion = 4
    val maxMessageSize = 10 * 1024 * 1024
    val maxTransactionSize = 10 * 1024 * 1024

    // todo: this makes the new network params active and nodes will fail because of ti
    @Transactional
    fun updateNetworkParams(notary: Node): nl.nuts.discovery.store.entity.NetworkParameters {
        // save node
        nodeRepository.save(notary)

        // latest or new
        var latest = networkParametersRepository.findFirstByOrderByIdDesc()
        if (latest == null) {
            latest = networkParametersRepository.save(
                nl.nuts.discovery.store.entity.NetworkParameters().apply {
                    modifiedTime = LocalDateTime.now()
                }
            )
        }

        // create corda variant for hash
        val cnp = cordaNetworkParameters(latest, listOf(notary))

        // sign and extract hash
        latest.hash = certificateAndKeyService.signNetworkParams(cnp).raw.hash.toString()
        latest.notaries = latest.notaries + notary

        // update record
        return networkParametersRepository.save(latest)
    }

    fun cordaNetworkParameters(params: nl.nuts.discovery.store.entity.NetworkParameters): NetworkParameters {
        return cordaNetworkParameters(params, emptyList())
    }

    private fun cordaNetworkParameters(params: nl.nuts.discovery.store.entity.NetworkParameters, additionalNotaries: List<Node>): NetworkParameters {
        return NetworkParameters(
            minPlatformVersion,
            (params.notaries + additionalNotaries).map { toNotaryInfo(it) },
            maxMessageSize,
            maxTransactionSize,
            params.modifiedTime!!.toInstant(ZoneOffset.systemDefault().getRules().getOffset(params.modifiedTime)),
            params.id!!,
            linkedMapOf()
        )
    }

    private fun toNotaryInfo(node: Node): NotaryInfo {
        val nodeInfo = node.toNodeInfo()

        // hardcoded non-validating
        return NotaryInfo(Party(nodeInfo.legalIdentitiesAndCerts.first().certificate), false)
    }
}