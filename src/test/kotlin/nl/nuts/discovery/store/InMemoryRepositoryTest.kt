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

package nl.nuts.discovery.store

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import net.corda.core.crypto.SecureHash
import net.corda.nodeapi.internal.SignedNodeInfo
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InMemoryRepositoryTest {

    lateinit var repo : InMemoryRepository

    @MockK
    lateinit var node: SignedNodeInfo

    @Before
    fun setup() {
        repo = InMemoryRepository()
        MockKAnnotations.init(this)
    }

    @Test
    fun `an added node can be retrieved by its hash`() {
        every {
            node.raw.hash
        } returns SecureHash.allOnesHash

        repo.addNode(node)

        assertNotNull(repo.nodeByHash(SecureHash.allOnesHash))
    }

    @Test
    fun `allNodes returns all nodes`() {
        every {
            node.raw.hash
        } returns SecureHash.allOnesHash

        repo.addNode(node)

        assertEquals(1, repo.allNodes().size)
    }

    @Test
    fun `getting a node with an unknown hash returns null`() {
        assertNull(repo.nodeByHash(SecureHash.randomSHA256()))
    }
}