/*
 *     Nuts discovery service for Corda network creation
 *     Copyright (C) 2020 Nuts community
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

package nl.nuts.discovery.store

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.store.entity.NetworkParameters
import nl.nuts.discovery.store.entity.Node
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@SpringBootTest
class NetworkParametersRepositoryTest {

    @Autowired
    lateinit var networkParametersRepository: NetworkParametersRepository

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var nodeRepository: NodeRepository

    val dt = LocalDateTime.now()
    val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")

    val np = NetworkParameters().apply {
        modifiedTime = dt
        hash = "hash"
    }

    private fun clear() {
        networkParametersRepository.deleteAll()
        nodeRepository.deleteAll()
    }

    @Before
    fun setup() {
        clear()

        val signedNodeInfo = TestUtils.subjectToSignedNodeInfo(certificateAndKeyService, subject)

        val n = nodeRepository.save(Node.fromNodeInfo(signedNodeInfo))
        np.notaries = listOf(n)
        networkParametersRepository.save(np)
    }
    @After
    fun teardown() {
        clear()
    }

    @Test
    fun `all fields validated`() {
        val nnp = networkParametersRepository.findFirstByOrderByIdDesc()
        assertNotNull(nnp)
        assertEquals(dt, nnp!!.modifiedTime)
        assertEquals("hash", nnp.hash)

        assertEquals(1, nnp.notaries.size)
        assertEquals(subject, CordaX500Name.parse(nnp.notaries.first().name!!))
    }

    @Test
    fun `find by hash`() {
        val nnp = networkParametersRepository.findByHash("hash")
        assertNotNull(nnp)
    }

    @Test
    fun `find last`() {
        val np2 = NetworkParameters().apply {
            modifiedTime = LocalDateTime.now()
            hash = "hash2"
        }

        networkParametersRepository.save(np2)

        val nnp = networkParametersRepository.findFirstByOrderByIdDesc()
        assertNotNull(nnp)
        assertEquals(0, nnp!!.notaries.size)
    }
}