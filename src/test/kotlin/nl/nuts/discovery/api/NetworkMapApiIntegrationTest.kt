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
import nl.nuts.discovery.store.InMemoryNodeRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


fun InMemoryNodeRepository.clear() { nodeInfoMap.clear() }
fun NetworkMapApi.clear() { (nodeRepository as InMemoryNodeRepository).clear() }

/**
 * Given all the crypto, it's easiest for now to use the LocalCertificateAndKeyService to do all the signing for us
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NetworkMapApiIntegrationTest {

    @Autowired
    lateinit var testRestTemplate : TestRestTemplate

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var networkMapApi: NetworkMapApi

    @Autowired
    lateinit var certificateService: CertificateAndKeyService

    @Before
    fun setup() {
        networkMapApi.clear()
    }

    @Test
    fun `signedNetworkMap is empty when no nodes are published`() {
        val response = testRestTemplate.getForEntity("/network-map", ByteArray::class.java)

        assertEquals(200, response.statusCodeValue)
        assertNotNull(response.body)

        val signedNetworkMap = response.body!!.deserialize<SignedNetworkMap>()
        assertTrue(signedNetworkMap.verified().nodeInfoHashes.isEmpty())
    }

    @Test
    fun `publishing a valid node returns 200`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")

        val resp = publishNode(subject)

        assertEquals(200, resp.statusCodeValue)
    }

    @Test
    fun `a published node is found in the network map`() {
        publishNode(CordaX500Name.parse("O=Org,L=Gr,C=NL"))

        val signedNetworkMap = networkMap()

        assertEquals(1, signedNetworkMap.verified().nodeInfoHashes.size)
    }

    @Test
    fun `a published node can be retrieved by its hash`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        publishNode(subject)

        val signedNetworkMap = networkMap()

        val hash = signedNetworkMap.verified().nodeInfoHashes.first()

        val response = testRestTemplate.getForEntity("/network-map/node-info/$hash", ByteArray::class.java)
        assertEquals(200, response.statusCodeValue)
        val signedNodeInfo = response.body!!.deserialize<SignedNodeInfo>()

        assertEquals(subject, signedNodeInfo.verified().legalIdentities.first().name)
    }

    @Test
    fun `Network params can be retrieved`() {
        val signedNetworkParams = networkParams()

        // simple check
        assertTrue(signedNetworkParams.verified().notaries.isEmpty())
    }

    @Test
    fun `Network params can be retrieved with a notary`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL,CN=notary")
        publishNode(subject)
        val signedNetworkParams = networkParams()
        val networkParameters = signedNetworkParams.verified()
        // simple check

        assertFalse(networkParameters.notaries.isEmpty())
        assertEquals(subject, networkParameters.notaries.first().identity.name)
    }

    private fun networkParams() : SignedNetworkParameters {
        val response = testRestTemplate.getForEntity("/network-map/network-parameters/1", ByteArray::class.java)
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

    private fun headers() : HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        return headers
    }

}