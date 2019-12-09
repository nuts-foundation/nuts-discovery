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

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.store.SignRequestStore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@RunWith(SpringRunner::class)
@SpringBootTest
class LocalCertificateAndKeyServiceTest {
    @Autowired
    lateinit var service: CertificateAndKeyService

    @Autowired
    lateinit var certService: CertificateAndKeyService

    @Autowired
    lateinit var signRequestStore: SignRequestStore

    @Before
    fun setup() {

    }

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
    fun `a request for signature is not automatically signed`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)

        signRequestStore.addSigningRequest(req)
        val certificate = signRequestStore.signedCertificate(subject)
        val pendingCertificate = signRequestStore.pendingSignRequest(subject)

        assertNull(certificate)
        assertNotNull(pendingCertificate)
    }

    @Test
    fun `a pending requests request can be retrieved`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)

        signRequestStore.addSigningRequest(req)
        val pendingRequests = signRequestStore.pendingSignRequests()
        assertEquals(pendingRequests.size, 1)
        assertEquals(subject, pendingRequests[0].legalName())
    }

    @Test
    fun `a signed signature can be retrieved`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        val signRequest = signRequestStore.addSigningRequest(req)
        service.signCertificate(req)
        signRequestStore.markAsSigned(signRequest)

        val certs = signRequestStore.signedCertificates()
        assertEquals(certs.size, 1)
        assertEquals(subject, certs[0].legalName())
    }

    @Test
    fun `service validates with existing certificates and keys`() {
        assertEquals(0, service.validate().size)
    }
}