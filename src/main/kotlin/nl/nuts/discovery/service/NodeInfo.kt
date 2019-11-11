package nl.nuts.discovery.service

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort


class NodeInfo(val legalEntity: CordaX500Name, val addresses: List<NetworkHostAndPort>){
}