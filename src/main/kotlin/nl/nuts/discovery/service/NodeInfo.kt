package nl.nuts.discovery.service

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort

data class NodeInfo(@JsonIgnore val nodeInfo: net.corda.core.node.NodeInfo) {

    @JsonProperty
    fun legalName(): CordaX500Name {
        return nodeInfo.legalIdentities[0].name
    }

    @JsonProperty
    fun addresses(): List<NetworkHostAndPort> {
        return nodeInfo.addresses
    }
}