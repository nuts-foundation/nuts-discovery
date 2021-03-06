package nl.nuts.discovery.service

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import net.corda.core.node.services.AttachmentId
import nl.nuts.discovery.store.NetworkParametersRepository
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.entity.Node
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import javax.transaction.Transactional

/**
 * Simple stateless NetworkParameters Service.
 * It ignores the given hashes and always returns the same version.
 * It can be used in development, small tests and early production networks.
 * In the later prod network, parameters updates will happen and multiple versions of
 * the parameters must be supported.
 */
@Transactional
@Service
class NetworkParametersService {

    companion object {
        const val SCHEMA_PACKAGE = "nl.nuts.consent.schema"
        const val FLOW_PACKAGE = "nl.nuts.consent.flow"
        const val CONTRACT_PACKAGE = "nl.nuts.consent.contract"
    }

    @Autowired
    lateinit var networkParametersRepository: NetworkParametersRepository

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Qualifier("customNodeRepository")
    @Autowired
    lateinit var nodeRepository: NodeRepository

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    /**
     * todo: Hardcoded values, if these change, stored hashes are no longer valid!
     */
    val minPlatformVersion = 4
    val maxMessageSize = 10 * 1024 * 1024
    val maxTransactionSize = 10 * 1024 * 1024

    /**
     * Post construct to make sure
     */
    @PostConstruct
    fun initialParams() {
        if (networkParametersRepository.findFirstByOrderByIdDesc() == null) {
            networkParametersRepository.save(createEmptyParams())
        }
    }

    // todo: this makes the new network params active and nodes will fail because of it
    /**
     * Update the network parameters with a new Notary. It'll also store the Notary node in the node repo
     */
    fun updateNetworkParams(notary: Node): nl.nuts.discovery.store.entity.NetworkParameters {
        // latest or new
        var latest = networkParametersRepository.findFirstByOrderByIdDesc()
        if (latest == null) {
            latest = networkParametersRepository.save(createEmptyParams())
        }

        // save node
        val savedNotary = nodeRepository.save(notary)

        // create corda variant for hash
        val cnp = cordaNetworkParameters(latest, listOf(savedNotary))

        // sign and extract hash
        latest.hash = certificateAndKeyService.signNetworkParams(cnp).raw.hash.toString()
        latest.notaries = (latest.notaries + savedNotary).toSet().toMutableList()

        // update record
        return networkParametersRepository.save(latest)
    }

    private fun createEmptyParams(): nl.nuts.discovery.store.entity.NetworkParameters {
        val np =  nl.nuts.discovery.store.entity.NetworkParameters().apply {
            modifiedTime = LocalDateTime.now()
        }

        // create corda variant for hash
        val cnp = cordaNetworkParameters(np, emptyList())

        // sign and extract hash
        np.hash = certificateAndKeyService.signNetworkParams(cnp).raw.hash.toString()

        return np
    }

    /**
     * create Corda networkParameters from the given entity
     */
    fun cordaNetworkParameters(params: nl.nuts.discovery.store.entity.NetworkParameters): NetworkParameters {
        return cordaNetworkParameters(params, emptyList())
    }

    private fun cordaNetworkParameters(params: nl.nuts.discovery.store.entity.NetworkParameters, additionalNotaries: List<Node>): NetworkParameters {

        val id = params.id ?: 1

        return NetworkParameters(
            minPlatformVersion,
            (params.notaries + additionalNotaries).toSet().map { toNotaryInfo(it) },
            maxMessageSize,
            maxTransactionSize,
            params.modifiedTime!!.toInstant(ZoneOffset.systemDefault().getRules().getOffset(params.modifiedTime)),
            id,
            whitelistedContractImplementations()
        )
    }

    private fun whitelistedContractImplementations(): HashMap<String, List<AttachmentId>> {
        val wci = LinkedHashMap<String, List<AttachmentId>>()
        val contractHashes = nutsDiscoveryProperties.contractHashes.split(",").map { it.trim() }
        val flowHashes = nutsDiscoveryProperties.flowHashes.split(",").map { it.trim() }

        val l = contractHashes.filter { it.isNotBlank() }.map { SecureHash.parse(it) }
        wci[CONTRACT_PACKAGE] = l
        wci[SCHEMA_PACKAGE] = l

        wci[FLOW_PACKAGE] = flowHashes.filter { it.isNotBlank() }.map { SecureHash.parse(it) }

        return wci
    }

    private fun toNotaryInfo(node: Node): NotaryInfo {
        val nodeInfo = node.toNodeInfo()

        // hardcoded non-validating
        return NotaryInfo(Party(nodeInfo.legalIdentitiesAndCerts.first().certificate), false)
    }
}