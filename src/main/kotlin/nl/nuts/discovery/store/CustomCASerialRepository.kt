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

package nl.nuts.discovery.store

import nl.nuts.discovery.store.entity.CASerial
import org.springframework.stereotype.Repository
import java.security.SecureRandom
import javax.transaction.Transactional

/**
 * Repository for CASerial entity with custom findOrCreate extension
 */
@Transactional
@Repository("customCASerialRepository")
class CustomCASerialRepository(private val delegate: CASerialRepository): CASerialRepository by delegate {

    /**
     * Combined access method for finding and creating a CASerial entity.
     * A CASerial needs to be set for each CA.
     */
    fun findOrCreateCASerial(subject: String): CASerial {
        var existing = delegate.findBySubject(subject)
        if (existing == null) {
            // generate salt
            val seed = SecureRandom.getSeed(32)
            existing = CASerial().apply {
                this.subject = subject
                this.salt = seed.toHexString()
            }
            delegate.save(existing)
        }

        return existing!!
    }
}

/**
 * Helper method to format a byte array to hex string
 */
fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }