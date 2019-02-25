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

import net.corda.core.crypto.SecureHash
import net.corda.nodeapi.internal.SignedNodeInfo
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

/**
 * A simple in-memory storage implementation of {@link nl.nuts.discovery.store.NodeRepository}
 */
@Profile(value = arrayOf("dev", "test", "default"))
@Service
class InMemoryRepository : NodeRepository {

    val nodeInfoMap = HashMap<SecureHash, SignedNodeInfo>()

    override fun addNode(signedNodeInfo: SignedNodeInfo) {
        nodeInfoMap[signedNodeInfo.raw.hash] = signedNodeInfo
    }

    override fun allNodes(): List<SignedNodeInfo> {
        return Collections.unmodifiableList(nodeInfoMap.entries.map { it.value })
    }

    override fun nodeByHash(hash: SecureHash): SignedNodeInfo? {
        return nodeInfoMap[hash]
    }

    override fun notary() : SignedNodeInfo? {
        return allNodes().firstOrNull{ it.verified().legalIdentities.any { it.name.commonName?.contains("notary")?: false } }
    }
}