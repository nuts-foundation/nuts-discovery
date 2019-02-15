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

import net.corda.core.CordaOID
import net.corda.core.crypto.Crypto
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.CertRole
import net.corda.nodeapi.internal.crypto.ContentSignerBuilder
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@SpringBootTest
class LocalCertificateAndKeyServiceTest {
    @Autowired
    lateinit var service : CertificateAndKeyService

    @Before
    fun setup() {

    }

    @Test
    fun `the intermediate certificate is loaded from test resources`() {
        assertNotNull(service.intermediateCertificate())
    }

    @Test
    fun `the root certificate is loaded from test resources`() {
        assertNotNull(service.rootCertificate())
    }

    @Test
    fun `the networkMap certificate is loaded from test resources`() {
        assertNotNull(service.networkMapCertificate())
    }

    @Test
    fun `a request for signature is automatically signed`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = createCertificateRequest(subject)

        service.submitSigningRequest(req)
        val certificate = service.signedCertificate(subject)

        assertNotNull(certificate)
        assertNotNull(certificate!!.signature)
    }

    private fun createCertificateRequest(subject : CordaX500Name) : PKCS10CertificationRequest {
        val signatureScheme = Crypto.RSA_SHA256
        val keyPair = Crypto.generateKeyPair(Crypto.RSA_SHA256)
        val email = "a@b.com"

        val signer = ContentSignerBuilder.build(signatureScheme, keyPair.private, Crypto.findProvider(signatureScheme.providerName))
        return JcaPKCS10CertificationRequestBuilder(subject.x500Principal, keyPair.public)
                .addAttribute(BCStyle.E, DERUTF8String(email))
                .addAttribute(ASN1ObjectIdentifier(CordaOID.X509_EXTENSION_CORDA_ROLE), CertRole.NODE_CA)
                .build(signer)
    }
}