package nl.nuts.discovery.service

import net.corda.core.identity.CordaX500Name
import java.io.Serializable

data class Node(val name: String, val approved: Boolean)
