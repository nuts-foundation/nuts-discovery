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

import nl.nuts.discovery.DiscoveryException
import nl.nuts.discovery.TestUtils.Companion.loadTestCSR
import nl.nuts.discovery.store.CertificateRepository
import nl.nuts.discovery.store.NutsCertificateRequestRepository
import nl.nuts.discovery.store.entity.NutsCertificateRequest
import nl.nuts.discovery.store.entity.NutsCertificateRequest.Companion.NUTS_VENDOR_OID
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@SpringBootTest
class CertificatesApiServiceImplTest {
    @Autowired
    lateinit var nutsCertificateRequestRepository: NutsCertificateRequestRepository

    @Autowired
    lateinit var certificateRepository: CertificateRepository


    @Autowired
    lateinit var certificatesApiServiceImpl: CertificatesApiServiceImpl

    @Before
    fun before() {
        nutsCertificateRequestRepository.deleteAll()
        certificateRepository.deleteAll()
    }

    @After
    fun teardown() {
        nutsCertificateRequestRepository.deleteAll()
        certificateRepository.deleteAll()
    }

    @Test
    fun `submitted CSR is stored`() {
        val pem = loadTestCSR("test.csr")

        certificatesApiServiceImpl.submit(pem)
        val csrs = nutsCertificateRequestRepository.findAll()

        assertEquals(1, csrs.count())
    }

    @Test
    fun `submitting invalid CSR raises`() {
        val pem = loadTestCSR("invalid.csr")

        assertFailsWith<DiscoveryException>("Invalid signature") {
            certificatesApiServiceImpl.submit(pem)
        }
    }

    @Test
    fun `submitted CSR can be listed`() {
        val pem = loadTestCSR("test.csr")

        nutsCertificateRequestRepository.save(NutsCertificateRequest.fromPEM(pem))
        val csrs = certificatesApiServiceImpl.listRequests("urn:oid:$NUTS_VENDOR_OID:1")

        assertEquals(1, csrs.count())
    }

    @Test
    fun `CSR can be signed`() {
        val pem = loadTestCSR("test.csr")
        val req = NutsCertificateRequest.fromPEM(pem)

        nutsCertificateRequestRepository.save(req)
        val x509 = certificatesApiServiceImpl.sign(req)

        assertNotNull(x509)

        val basicConstraints = x509.basicConstraints

        assertEquals(Integer.MAX_VALUE, basicConstraints) // no path constraint for this cert

        // Assert that the PublicKey in the CSR ends up in the certificate
        val actualPublicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(req.toPKCS10().subjectPublicKeyInfo.encoded))
        assertEquals(x509.publicKey, actualPublicKey)

        certificateRepository.findAll().forEach {
            assertEquals("urn:oid:$NUTS_VENDOR_OID:1", it.oid.toString())
        }
    }
}