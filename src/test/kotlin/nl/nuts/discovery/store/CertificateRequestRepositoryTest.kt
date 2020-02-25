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
import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.store.entity.CertificateRequest
import nl.nuts.discovery.store.entity.Node
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(SpringRunner::class)
@SpringBootTest
class CertificateRequestRepositoryTest {

    @Autowired
    lateinit var certificateRequestRepository: CertificateRequestRepository

    @Before
    fun setup() {
        certificateRequestRepository.deleteAll()
    }

    @Test
    fun `an added node can be retrieved by its name`() {
        val dt = LocalDateTime.now()
        val request = CertificateRequest().apply {
            name = "name"
            submittedAt = dt
            pkcs10Csr = "bytes".toByteArray()

        }
        certificateRequestRepository.save(request)

        val sr = certificateRequestRepository.findByName("name")

        assertNotNull(sr)
        assertEquals(dt, sr!!.submittedAt)
    }
}