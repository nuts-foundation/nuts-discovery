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
import org.springframework.web.bind.annotation.*

/**
 * Admin API handles REST calls for an administrator UI.
 */
@CrossOrigin
@RestController
@RequestMapping("/admin")
class AdminApi {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var nodeRepo: NodeRepository

    /**
     * Handle the GET request for all sign-requests. Returns a json array of SignRequests
     */
    @RequestMapping("/certificates/signrequests", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListSignRequests(): ResponseEntity<List<SignRequest>> {
        logger.debug("listing pending sign requests")
        val list = certificateAndKeyService.pendingSignRequests()
        return ResponseEntity(list, HttpStatus.OK)
    }

    /**
     * Handle the GET request for all signed certificates in the network. Returns an
     */
    @RequestMapping("/certificates", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListCertificates(): ResponseEntity<List<SignRequest>> {
        logger.debug("listing signed certificates")
        val list = certificateAndKeyService.signedCertificates()
        return ResponseEntity(list, HttpStatus.OK)
    }

    /**
     * Handle Node approval request.
     */
    @RequestMapping("/certificates/signrequests/{nodeId}/approve", method = [RequestMethod.PUT])
    fun handleApproveSignRequest(@PathVariable("nodeId") nodeId: String): ResponseEntity<Any> {
        logger.debug("received sign request for: $nodeId")
        try {
            val subject = CordaX500Name.parse(nodeId)
            certificateAndKeyService.signAndAddCertificate(subject)
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.ok().build()
    }

    /**
     * Handle GET request for the network-map. Returns a json network map.
     */
    @RequestMapping("/network-map", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListNodes(): ResponseEntity<List<nl.nuts.discovery.service.NodeInfo>> {
        logger.debug("listing network map status")
        val list = nodeRepo.allNodes().map { nl.nuts.discovery.service.NodeInfo(it.verified()) }
        return ResponseEntity(list, HttpStatus.OK)
    }

}