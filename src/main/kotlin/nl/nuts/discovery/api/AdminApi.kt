package nl.nuts.discovery.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.serialize
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.Node
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

    @RequestMapping("/nodes/pending", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListPendingRequests(): ResponseEntity<List<Node>> {
        logger.debug("listing pending node requests")
        val list = certificateAndKeyService.pendingNodes()
        return ResponseEntity(list, HttpStatus.OK)
    }

    @RequestMapping("/nodes", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListNodes(): ResponseEntity<List<Node>>{
        logger.debug("listing signed nodes")
        val list = certificateAndKeyService.signedNodes()
        return ResponseEntity(list, HttpStatus.OK)
    }

    @RequestMapping("/nodes/{nodeId}/approve")
    fun handleApproveNodeRequest(@PathVariable("nodeId") nodeId: String): ResponseEntity<Any> {
        logger.debug("received approve request for: $nodeId")
        try {
            val subject = CordaX500Name.parse(nodeId)
            certificateAndKeyService.signCertificate(subject)
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.ok().build()
    }
}