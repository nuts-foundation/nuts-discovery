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

import nl.nuts.discovery.store.entity.NutsCertificateRequest
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
class NutsCertificateRequestRepositoryTest {

    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    @Before
    fun setup() {
        nutsCertificateRequestRepository.deleteAll()
    }

    @Test
    fun `an added node can be retrieved by its oid`() {
        val dt = LocalDateTime.now()
        val request = NutsCertificateRequest().apply {
            name = "name"
            submittedAt = dt
            oid = "kvk"
            pem = "pem encoded bytes"

        }
        nutsCertificateRequestRepository.save(request)

        val sr = nutsCertificateRequestRepository.findByOid("kvk")

        assertNotNull(sr)
        assertEquals(dt, sr!!.submittedAt)
    }
}