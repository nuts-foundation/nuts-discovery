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

package nl.nuts.discovery.service

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@SpringBootTest
class LocalCertificateAndKeyServiceTest {

    @Autowired
    lateinit var certService: CertificateAndKeyService

    @Test
    fun `the intermediate certificate is loaded from test resources`() {
        assertNotNull(certService.intermediateCertificate())
    }

    @Test
    fun `the root certificate is loaded from test resources`() {
        assertNotNull(certService.rootCertificate())
    }

    @Test
    fun `the networkMap certificate is loaded from test resources`() {
        assertNotNull(certService.networkMapCertificate())
    }

    @Test
    fun `service validates with existing certificates and keys`() {
        assertEquals(0, certService.validate().size)
    }

}