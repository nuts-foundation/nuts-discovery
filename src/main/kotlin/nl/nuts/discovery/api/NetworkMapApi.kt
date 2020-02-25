/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2019 Nuts community
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package nl.nuts.discovery.api

import net.corda.core.crypto.SecureHash
import net.corda.core.internal.readObject
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkParameters
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.NetworkParametersService
import nl.nuts.discovery.store.NodeRepository
import nl.nuts.discovery.store.entity.Node
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

/**
 * This api should be behind TLS two-way ssl using the Corda TLS keystore....
 *
 * Taken from net.corda.testing.node.internal.network.NetworkMapServer
 */
@RestController
@RequestMapping("/network-map", produces = arrayOf("*/*"), consumes = arrayOf("*/*"))
class NetworkMapApi {
    val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    @Qualifier("customNodeRepository")
    lateinit var nodeRepository: NodeRepository

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var networkParametersService: NetworkParametersService

    /**
     * Accept NodeInfo from connecting nodes.
     */
    @RequestMapping("/publish", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
    fun publishNodeInfo(@RequestBody input: ByteArray): ResponseEntity<ByteArray> {
        try {
            val signedNodeInfo = ByteArrayInputStream(input).readObject<SignedNodeInfo>()

            //verify
            val node = Node.fromNodeInfo(signedNodeInfo)
            logger.info("received a publish request for legalIdentities: {}", node.name)

            nodeRepository.save(node)
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw e
        }

        return ResponseEntity.ok("".toByteArray())
    }

    /**
     * Currently not supported
     */
    @RequestMapping("/ack-parameters", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
    fun ackNetworkParameters(@RequestBody input: InputStream): ResponseEntity<Any> {

        return ResponseEntity.ok().build()
    }

    /**
     * Retrieve the global networkMap signed with the network map key.
     */
    @RequestMapping("", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
    fun getGlobalNetworkMap(): ResponseEntity<ByteArray> {

        try {
            val nodeListHashes = nodeRepository.findAll().map { SecureHash.parse(it.hash) }
            val signedNetworkParameters = signedNetworkParams()
            val networkMap = NetworkMap(nodeListHashes, signedNetworkParameters.raw.hash, null)
            val signedNetworkMap = certificateAndKeyService.signNetworkMap(networkMap)

            return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .body(signedNetworkMap.serialize().bytes)
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw e
        }
    }

    /**
     * Currently not implemented
     */
    @RequestMapping("/{var}", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
    fun getPrivateNetworkMap(@PathVariable("var") extraUUID: String): ResponseEntity<InputStream> {

        return ResponseEntity.ok("".byteInputStream(Charsets.UTF_8))
    }

    /**
     * Retrieve the NodeInfo object published by the node. Index is the secure hash of the nodeInfo object.
     */
    @RequestMapping("node-info/{var}", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
    fun getNodeInfo(@PathVariable("var") nodeInfoHash: String): ResponseEntity<ByteArray> {
        val node = nodeRepository.findByHash(nodeInfoHash)

        return if (node != null) {
            ResponseEntity.ok(node.toSignedNodeInfo().serialize().bytes)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    /**
     * Get the network parameters based on its hash.
     * Right now it returns the latest params and doesn't support any others yet.
     */
    @RequestMapping("network-parameters/{var}", method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_OCTET_STREAM_VALUE))
    fun getNetworkParameter(@PathVariable("var") hash: String): ResponseEntity<ByteArray> {
        return ResponseEntity.ok(signedNetworkParams().serialize().bytes)
    }

    private fun signedNetworkParams(notary: X509Certificate?): SignedNetworkParameters {
        val networkParameters = networkParametersService.networkParameters(null)
        return certificateAndKeyService.signNetworkParams(networkParameters)
    }

    private fun signedNetworkParams(): SignedNetworkParameters {
        return signedNetworkParams(
            nodeRepository
                .findByNameContaining("notary")
                ?.toNodeInfo()
                ?.legalIdentitiesAndCerts
                ?.first()
                ?.certificate
        )
    }
}