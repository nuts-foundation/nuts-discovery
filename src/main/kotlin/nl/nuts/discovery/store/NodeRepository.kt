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

interface NodeRepository {
    /**
     * Add information about a node to the store.
     *
     * @param signedNodeInfo the SignedNodeInfo object received at the NetworkMap api.
     */
    fun addNode(signedNodeInfo: SignedNodeInfo)

    /**
     * @return an ImmutableList of all added nodes.
     */
    fun allNodes() : List<SignedNodeInfo>

    /**
     * @param hash the secure hash of the raw NodeInfo object.
     *
     * @return the signedNodeInfo if found, null otherwise.
     */
    fun nodeByHash(hash:SecureHash) : SignedNodeInfo?

    /**
     * Find the first node with the name notary in the CN
     *
     * @return SignedNodeInfo of the first found Notary
     */
    fun notary() : SignedNodeInfo?
}
