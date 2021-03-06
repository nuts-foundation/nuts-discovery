package nl.nuts.discovery.api

import nl.nuts.discovery.model.CertificateSigningRequest
import nl.nuts.discovery.model.CertificateWithChain
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.validation.annotation.Validated
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Autowired

import javax.validation.Valid
import javax.validation.constraints.*

import kotlin.collections.List
import kotlin.collections.Map

@Controller
@Validated
@RequestMapping("\${api.base-path:}")
class CertificatesApiController(@Autowired(required = true) val service: CertificatesApiService) {


    @RequestMapping(
            value = ["/api/x509"],
            produces = ["application/json"], 
            method = [RequestMethod.GET])
    fun listCertificates(@NotNull  @RequestParam(value = "otherName", required = true, defaultValue="null") otherName: String): ResponseEntity<List<CertificateWithChain>> {
        return ResponseEntity(service.listCertificates(otherName), HttpStatus.OK)
    }


    @RequestMapping(
            value = ["/api/csr"],
            produces = ["application/json"], 
            method = [RequestMethod.GET])
    fun listRequests(@NotNull  @RequestParam(value = "otherName", required = true, defaultValue="null") otherName: String): ResponseEntity<List<CertificateSigningRequest>> {
        return ResponseEntity(service.listRequests(otherName), HttpStatus.OK)
    }


    @RequestMapping(
            value = ["/api/csr"],
            produces = ["application/json", "text/plain"], 
            consumes = ["text/plain"],
            method = [RequestMethod.POST])
    fun submit( @Valid @RequestBody body: String): ResponseEntity<CertificateSigningRequest> {
        return ResponseEntity(service.submit(body), HttpStatus.OK)
    }
}
