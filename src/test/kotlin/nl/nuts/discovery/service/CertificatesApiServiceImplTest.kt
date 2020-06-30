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

import nl.nuts.discovery.TestUtils.Companion.loadTestCSR
import nl.nuts.discovery.store.NutsCertificateRequestRepository
import nl.nuts.discovery.store.entity.NutsCertificateRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest
class CertificatesApiServiceImplTest {
    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    @Autowired
    lateinit var certificatesApiServiceImpl: CertificatesApiServiceImpl

    @Before
    fun before() {
        nutsCertificateRequestRepository.deleteAll()
    }

    @After
    fun teardown() {
        nutsCertificateRequestRepository.deleteAll()
    }

    @Test
    fun `submitted csr is stored`() {
        val pem = loadTestCSR("test.csr")

        certificatesApiServiceImpl.submit(pem)
        val csrs = nutsCertificateRequestRepository.findAll()

        assertEquals(1, csrs.count())
    }

    @Test
    fun `submitted csr can be listed`() {
        val pem = loadTestCSR("test.csr")

        nutsCertificateRequestRepository.save(NutsCertificateRequest.fromPEM(pem))
        val csrs = certificatesApiServiceImpl.listRequests("urn:oid:kvk")

        assertEquals(1, csrs.count())
    }
}