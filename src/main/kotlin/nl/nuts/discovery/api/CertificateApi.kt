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
@RequestMapping("/certificate", produces = arrayOf("*/*") ,consumes = arrayOf("*/*"))
class CertificateApi {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    @RequestMapping("", method = arrayOf(RequestMethod.POST), produces = arrayOf("*/*") ,consumes = arrayOf("*/*"))
    fun handleCertificateRequest(@RequestBody input: ByteArray,
                                 @RequestHeader("Platform-Version") platformVersion: String,
                                 @RequestHeader("Client-Version") clientVersion: String,
                                 @RequestHeader("Private-Network-Map", required = false) pnm: String?) : ResponseEntity<String> {
        try {
            val pkcs10Request = PKCS10CertificationRequest(input)

            logger.info("Received request for: ${pkcs10Request.subject}")
            logger.info("Platform-Version: $platformVersion")
            logger.info("Client-Version: $clientVersion")
            logger.info("Private-Network-Map: $pnm")

            certificateAndKeyService.submitSigningRequest(pkcs10Request)

            return ResponseEntity.ok(pkcs10Request.subject.toString())
        } catch (e:Exception) {
            logger.error(e.message, e)
            return ResponseEntity.badRequest().build()
        }
    }

    @RequestMapping("/{var}", method = arrayOf(RequestMethod.GET))
    fun downloadCertificate(@PathVariable("var") requestId: String) : ResponseEntity<ByteArray> {
        try {
            logger.info("Received certificate download request for: $requestId")
            val name = CordaX500Name.parse(requestId)

            // certificate signed?
            val certificate = certificateAndKeyService.signedCertificate(name)
                // certificate pending?
                ?: return if (certificateAndKeyService.pendingCertificate(name) != null) {
                    // try later
                    ResponseEntity.noContent().build()
                } else {
                    // nope, don't try again.
                    ResponseEntity.status(403).build()
                }

            val certPath = X509Utilities.buildCertPath(certificate, certificateAndKeyService.intermediateCertificate(), certificateAndKeyService.rootCertificate())

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