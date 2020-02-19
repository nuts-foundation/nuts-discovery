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

package nl.nuts.discovery.store.entity

import net.corda.core.crypto.DigitalSignature
import net.corda.core.internal.readObject
import net.corda.core.node.NodeInfo
import net.corda.core.serialization.SerializedBytes
import net.corda.nodeapi.internal.SignedNodeInfo
import java.io.ByteArrayInputStream
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany


@Entity
class Node {

    companion object {
        fun fromNodeInfo(nodeInfo: SignedNodeInfo): Node {
            return Node().apply {
                hash = nodeInfo.raw.hash.toString()
                name = nodeInfo.verified().legalIdentities.firstOrNull()?.name.toString()
                raw = nodeInfo.raw.bytes
                signatures = nodeInfo.signatures.map { Signature.from(it) }
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
    var hash: String? = null
    /**
     * CN of certificate, like X500Name
     */
    var name: String? = null
    var raw: ByteArray? = null
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "node_id")
    var signatures: List<Signature> = mutableListOf()

    fun toNodeInfo(): NodeInfo {
        return ByteArrayInputStream(raw).readObject()
    }

    fun toSignedNodeInfo(): SignedNodeInfo {
        val sigs: List<DigitalSignature> = signatures.map{DigitalSignature(it.raw!!) }
        return SignedNodeInfo(SerializedBytes(raw!!), sigs)
    }
}