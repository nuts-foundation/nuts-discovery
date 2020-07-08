/*
 *     Nuts discovery service
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

package nl.nuts.discovery.service

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Temporary class to abstract away from common logic found in both Certificate service classes
 */
abstract class AbstractCertificatesService {
    protected fun loadResourceWithNullCheck(location: String): Path {

        if (File(location).exists()) {
            return Paths.get(File(location).toURI())
        }

        val resource = javaClass.classLoader.getResource(location)
            ?: throw IllegalArgumentException("resource not found at $location")

        val uri = resource.toURI()
        return Paths.get(uri)
    }
}