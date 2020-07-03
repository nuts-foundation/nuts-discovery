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

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.store.entity.Certificate
import nl.nuts.discovery.store.entity.CertificateRequest
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
class CertificateRepositoryTest {

    @Autowired
    lateinit var certificateRepository: CertificateRepository

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Before
    fun setup() {
        certificateRepository.deleteAll()
    }

    @Test
    fun `an added certificate can be retrieved by its name`() {
        val request = Certificate().apply {
            name = "name"
            x509 = "bytes".toByteArray()

        }
        certificateRepository.save(request)

        val sr = certificateRepository.findByName("name")

        assertNotNull(sr)
    }

    @Test
    fun `countByName gives the correct count`() {
        val request = Certificate().apply {
            name = "name"
            x509 = "bytes".toByteArray()
            ca = "ca"
        }
        certificateRepository.save(request)

        val count = certificateRepository.countByCa("ca")

        assertEquals(1, count)
    }

    @Test
    fun `a certificate can be converted to X509`() {
        val x500 = CordaX500Name.parse("C=NL,L=Gr,O=Org")
        val pkcs10 = TestUtils.createCertificateRequest(x500)
        val x509 = certificateAndKeyService.signCertificate(CertificateRequest.fromPKCS10(pkcs10))

        val cert = Certificate.fromX509Certificate(x509, "", "")
        val x509v2 = cert.toX509()

        assertEquals(x509, x509v2)
    }
}