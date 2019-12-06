package nl.nuts.discovery.service

import net.corda.core.node.NetworkParameters

interface NetworkParametersService {

    /**
     * Return network-parameters for a given versionHash.
     * If no versionHash is given, it returns the latest current network-parameters
     */
    fun networkParameters(versionHash: String?) : NetworkParameters
}