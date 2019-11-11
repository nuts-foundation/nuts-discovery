package nl.nuts.discovery.api

import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NodeInfo
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.SignRequest
import nl.nuts.discovery.store.NodeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminApi {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var nodeRepo: NodeRepository

    @RequestMapping("/certificates/signrequests", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListSignRequests(): ResponseEntity<List<SignRequest>> {
        logger.debug("listing pending sign requests")
        val list = certificateAndKeyService.pendingSignRequests()
        return ResponseEntity(list, HttpStatus.OK)
    }

    @RequestMapping("/certificates", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListCertificates(): ResponseEntity<List<SignRequest>> {
        logger.debug("listing signed certificates")
        val list = certificateAndKeyService.signedCertificates()
        return ResponseEntity(list, HttpStatus.OK)
    }

    @RequestMapping("/certificates/signrequests/{nodeId}/approve")
    fun handleApproveSignRequest(@PathVariable("nodeId") nodeId: String): ResponseEntity<Any> {
        logger.debug("received sign request for: $nodeId")
        try {
            val subject = CordaX500Name.parse(nodeId)
            certificateAndKeyService.signCertificate(subject)
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.ok().build()
    }

    @RequestMapping("/network-map", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListNodes(): ResponseEntity<List<nl.nuts.discovery.service.NodeInfo>> {
        logger.debug("listing network map status")
        val list = nodeRepo.allNodes().map { it.verified() }.map { nl.nuts.discovery.service.NodeInfo(it.legalIdentities.first().name, it.addresses) }
        return ResponseEntity(list, HttpStatus.OK)
    }

}