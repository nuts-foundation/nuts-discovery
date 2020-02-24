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

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.OneToMany

/**
 * Entity representing current and future network parameters
 */
@Entity
class NetworkParameters {

    /**
     * Doubles as epoch value in NetworkParameters
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    /**
     * Hash of raw bytes, since we do not store the raw bytes, hardcoded values can't change
     */
    var hash: String? = null

    @Column(name = "modified_time")
    var modifiedTime: LocalDateTime? = null

    @OneToMany(fetch=FetchType.EAGER)
    @JoinTable(name="network_parameters_nodes", joinColumns=[JoinColumn(name="network_parameters_id")], inverseJoinColumns=[JoinColumn(name="node_id")] )
    var notaries: List<Node> = mutableListOf()
}