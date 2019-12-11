package nl.nuts.discovery.store

import net.corda.core.identity.CordaX500Name
import nl.nuts.discovery.TestUtils
import nl.nuts.discovery.service.CertificateAndKeyService
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
class InMemorySignRequestStoreTest {

    @Autowired
    lateinit var signRequestStore: SignRequestStore

    @Autowired
    lateinit var certService: CertificateAndKeyService

    @Test
    fun `a signed signature can be retrieved`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)
        val signRequest = signRequestStore.addSigningRequest(req)
        certService.signCertificate(req)
        signRequestStore.markAsSigned(signRequest)

        val certs = signRequestStore.signedCertificates()
        assertEquals(certs.size, 1)
        assertEquals(subject, certs[0].legalName())
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
    fun `a request for signature is not automatically signed`() {
        val subject = CordaX500Name.parse("O=Org,L=Gr,C=NL")
        val req = TestUtils.createCertificateRequest(subject)

        signRequestStore.addSigningRequest(req)
        val certificate = signRequestStore.signedCertificate(subject)
        val pendingCertificate = signRequestStore.pendingSignRequest(subject)

        assertNull(certificate)
        assertNotNull(pendingCertificate)
    }

}