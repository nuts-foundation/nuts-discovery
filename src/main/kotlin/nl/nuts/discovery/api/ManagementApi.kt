package nl.nuts.discovery.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/manage")
class ManagementApi {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/pending", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleListPendingRequests(): ResponseEntity<String> {
        logger.debug("listing pending access requests")
        return ResponseEntity.ok("ok")
    }

}