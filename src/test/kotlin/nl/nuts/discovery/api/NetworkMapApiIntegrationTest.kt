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

import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.deserialize
import net.corda.core.serialization.serialize
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.network.SignedNetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkParameters
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.clear
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


fun NetworkMapApi.clear() {
    networkParametersService.clear()
}

/**
 * Given all the crypto, it's easiest for now to use the LocalCertificateAndKeyService to do all the signing for us
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NetworkMapApiIntegrationTest {

    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Autowired
    lateinit var networkMapApi: NetworkMapApi

    @Autowired
    lateinit var certificateService: CertificateAndKeyService

    val subjectNotary = CordaX500Name.parse("O=Org,L=Gr,C=NL,CN=notary")
    val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")

    @Before
    fun setup() {
        networkMapApi.clear()
    }

    @After
    fun teardown() {
        networkMapApi.clear()
    }

    @Test
    fun `signedNetworkMap returns 200 when no notaries are published`() {
        val response = testRestTemplate.getForEntity("/network-map", ByteArray::class.java)

        assertEquals(200, response.statusCodeValue)
    }

    @Test
    fun `publishing a valid node returns 200`() {
        val resp = publishNode(subject)

        assertEquals(200, resp.statusCodeValue)
    }

    @Test
    fun `publishing a valid node twice returns 200`() {
        publishNode(subject)
        val resp = publishNode(subject)

        assertEquals(200, resp.statusCodeValue)
    }

    @Test
    fun `a published node is found in the network map`() {
        publishNotary()
        publishNode(subject)

        val signedNetworkMap = networkMap()

        assertEquals(2, signedNetworkMap.verified().nodeInfoHashes.size)
    }

    @Test
    fun `a published node can be retrieved by its hash`() {
        publishNotary()

        val signedNetworkMap = networkMap()

        val hash = signedNetworkMap.verified().nodeInfoHashes.first()

        val response = testRestTemplate.getForEntity("/network-map/node-info/$hash", ByteArray::class.java)
        assertEquals(200, response.statusCodeValue)
        val signedNodeInfo = response.body!!.deserialize<SignedNodeInfo>()

        assertEquals(subjectNotary, signedNodeInfo.verified().legalIdentities.first().name)
    }

    @Test
    fun `Network params hash is the same`() {
        publishNotary()
        val networkMap = networkMap()
        val listedHash = networkMap.verified().networkParameterHash
        val signedNetworkParams = networkParams(listedHash.toString())
        val calcHash = signedNetworkParams.raw.hash

        // simple check
        assertEquals(listedHash, calcHash)
    }

    @Test
    fun `Network params can be retrieved with a notary`() {
        publishNotary()
        val networkMap = networkMap()
        val networkParams = networkParams(networkMap.verified().networkParameterHash.toString()).verified()

        assertFalse(networkParams.notaries.isEmpty())
        assertEquals(subjectNotary, networkParams.notaries.first().identity.name)
    }

    private fun networkParams(hash: String) : SignedNetworkParameters {
        val response = testRestTemplate.getForEntity("/network-map/network-parameters/$hash", ByteArray::class.java)
        assertEquals(200, response.statusCodeValue)
        return response.body!!.deserialize()
    }

    private fun networkMap() : SignedNetworkMap {
        val response = testRestTemplate.getForEntity("/network-map", ByteArray::class.java)
        assertEquals(200, response.statusCodeValue)
        return response.body!!.deserialize()
    }

    private fun publishNode(subject : CordaX500Name) :ResponseEntity<ByteArray>  {
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(certificateService, subject)
        val entity = HttpEntity(signedNodeInfo.serialize().bytes, headers())
        return  testRestTemplate.exchange("/network-map/publish", HttpMethod.POST, entity, ByteArray::class.java)
    }

    private fun publishNotary() :ResponseEntity<ByteArray>  {
        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(certificateService, subjectNotary)
        val entity = HttpEntity(signedNodeInfo.serialize().bytes, headers())
        return  testRestTemplate.exchange("/network-map/publish", HttpMethod.POST, entity, ByteArray::class.java)
    }

    private fun headers() : HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        return headers
    }

}