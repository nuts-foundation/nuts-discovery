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

package nl.nuts.discovery.api

import net.corda.core.identity.CordaX500Name
import net.corda.nodeapi.internal.crypto.X509Utilities
import nl.nuts.discovery.service.CertificateAndKeyService
import nl.nuts.discovery.service.NutsDiscoveryProperties
import nl.nuts.discovery.store.CertificateRepository
import nl.nuts.discovery.store.CertificateRequestRepository
import nl.nuts.discovery.store.entity.CertificateRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Certificate API for handling Certificate Signing Requests and returning an answer when signed.
 */
@RestController
@RequestMapping("/doorman/certificate", produces = ["*/*"], consumes = arrayOf("*/*"))
class CertificateApi {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @Autowired
    lateinit var certificateRequestRepository: CertificateRequestRepository

    @Autowired
    lateinit var certificateRepository: CertificateRepository

    @Autowired
    lateinit var nutsDiscoveryProperties: NutsDiscoveryProperties

    @RequestMapping("", method = arrayOf(RequestMethod.POST), produces = arrayOf("*/*"), consumes = arrayOf("*/*"))
    fun handleCertificateRequest(@RequestBody input: ByteArray,
                                 @RequestHeader("Platform-Version") platformVersion: String,
                                 @RequestHeader("Client-Version") clientVersion: String,
                                 @RequestHeader("Private-Network-Map", required = false) pnm: String?): ResponseEntity<String> {
        return try {
            val pkcs10Request = PKCS10CertificationRequest(input)

            logger.info("Received request for: ${pkcs10Request.subject}")
            logger.info("Platform-Version: $platformVersion")
            logger.info("Client-Version: $clientVersion")
            logger.info("Private-Network-Map: $pnm")

            val req = certificateRequestRepository.save(CertificateRequest.fromPKCS10(pkcs10Request))
            if (nutsDiscoveryProperties.autoAck) {
                certificateAndKeyService.signCertificate(req)
            }

            ResponseEntity.ok(pkcs10Request.subject.toString())
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.badRequest().build()
        }
    }

    @RequestMapping("/{var}", method = arrayOf(RequestMethod.GET))
    fun downloadCertificate(@PathVariable("var") requestId: String): ResponseEntity<ByteArray> {
        try {
            logger.info("Received certificate download request for: $requestId")
            var x500Name = CordaX500Name.parse(requestId) // for sorting

            // certificate signed?
            val certificate = certificateRepository.findByName(x500Name.toString())
            // certificate pending?
                ?: return if (certificateRequestRepository.findByName(x500Name.toString()) != null) {
                    // try later
                    ResponseEntity.noContent().build()
                } else {
                    // nope, don't try again.
                    ResponseEntity.status(403).build()
                }

            val certPath = X509Utilities.buildCertPath(certificate.toX509(), certificateAndKeyService.intermediateCertificate(), certificateAndKeyService.rootCertificate())

            val baos = ByteArrayOutputStream()
            ZipOutputStream(baos as OutputStream?).use { zip ->
                listOf(X509Utilities.CORDA_CLIENT_CA, X509Utilities.CORDA_INTERMEDIATE_CA, X509Utilities.CORDA_ROOT_CA).zip(certPath.certificates).forEach {
                    zip.putNextEntry(ZipEntry("${it.first}.cer"))
                    zip.write(it.second.encoded)
                    zip.closeEntry()
                }
            }
            return ResponseEntity
                .ok()
                //.contentType(MediaType.parseMediaType("application/zip"))
                .header("Content-Disposition", "attachment; filename=\"certificates.zip\"")
                .body(baos.toByteArray())

        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.badRequest().build()
        }
    }
}