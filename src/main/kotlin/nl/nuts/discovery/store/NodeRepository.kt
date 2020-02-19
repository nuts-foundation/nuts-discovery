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

import nl.nuts.discovery.store.entity.Node
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeRepository: CrudRepository<Node, Long> {
    /** @param hash the secure hash of the raw NodeInfo object. As hex string
     *
     * @return the signedNodeInfo if found, null otherwise.
     */
    fun findByHash(hash:String) : Node?

    /**
     * Find the first node with the specified name in the CN
     *
     * @return Node of the first found node
     */
    fun findByNameContaining(name: String) : Node?

    /**
     * Find the node with the specified name as CN
     *
     * @return Node
     */
    fun findByName(name: String) : Node?
}
