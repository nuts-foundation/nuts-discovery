package nl.nuts.discovery.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort

/**
 * NodeInfo wraps a corda.Node and provides a way to serialize it.
 */
data class NodeInfo(@JsonIgnore val nodeInfo: net.corda.core.node.NodeInfo) {

    /**
     * Returns the CordaX500Name from the first legalIdentity
     */
    @JsonProperty
    fun legalName(): CordaX500Name {
        return nodeInfo.legalIdentities[0].name
    }

    /**
     * Returns the tcp addresses the node is connected to.
     */
    @JsonProperty
    fun addresses(): List<NetworkHostAndPort> {
        return nodeInfo.addresses
    }
}