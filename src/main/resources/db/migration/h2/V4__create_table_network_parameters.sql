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

CREATE TABLE network_parameters
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    hash          VARCHAR(64) UNIQUE,
    modified_time TIMESTAMP   NOT NULL
);

CREATE TABLE network_parameters_nodes
(
    network_parameters_id INT,
    node_id               INT,
    FOREIGN KEY (network_parameters_id) references network_parameters (id),
    FOREIGN KEY (node_id) references node (id)
);
