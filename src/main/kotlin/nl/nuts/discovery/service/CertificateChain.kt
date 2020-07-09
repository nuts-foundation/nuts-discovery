/*
 *     Nuts discovery service
 *     Copyright (C) 2020 Nuts community
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

import net.corda.core.internal.readText
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.util.io.pem.PemObject
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Path

/**
 * Helper class to load x509 certificates and combine them as needed.
 *
 * @param paths locations of PEM files, starting with lowest CA and ending with the root
 */
class CertificateChain(var pemEncodedCertificates: List<String> = mutableListOf()) {

    companion object {
        /**
         * constructs a CertificateChain holder from file locations
         */
        fun fromPaths(paths: Array<Path>): CertificateChain {
            val l = paths.map {
                it.readText()
            }
            return CertificateChain(l)
        }

        /**
         * constructs a CertificateChain holder from a single PEM file
         */
        fun fromSinglePEM(cChain: String?) : CertificateChain {
            val cList = mutableListOf<String>()



            if (cChain != null) {
                val parser = PEMParser(StringReader(cChain))

                var nextObject = parser.readPemObject()
                while (nextObject != null) {
                    val sw = StringWriter()
                    val writer = JcaPEMWriter(sw)
                    writer.writeObject(PemObject("CERTIFICATE", nextObject.content))
                    cList.add(sw.toString())
                    nextObject = parser.readPemObject()
                }
            }

            return CertificateChain(cList)
        }
    }

    /**
     * Outputs the set of files for this chain as a single PEM
     */
    fun asSinglePEM() : String {
        val out = ByteArrayOutputStream()

        for ((index, it) in pemEncodedCertificates.withIndex()) {
            out.write(it.toByteArray())
            if (index < pemEncodedCertificates.size - 1) {
                out.write("\n".toByteArray())
            }
        }

        return out.toString(Charsets.UTF_8.name())
    }
}