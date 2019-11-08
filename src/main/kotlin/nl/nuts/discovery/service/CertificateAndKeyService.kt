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

import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NetworkParameters
import net.corda.nodeapi.internal.network.NetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkMap
import net.corda.nodeapi.internal.network.SignedNetworkParameters
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.security.cert.X509Certificate

interface CertificateAndKeyService {
    /**
     * Submit a new CSR for the signing process.
     */
    fun submitSigningRequest(request: PKCS10CertificationRequest)

    /**
     * Sign a certificate based on the name
     */
    fun signCertificate(serial: CordaX500Name) : X509Certificate?

    /**
     * Retrieve a signed certificate based on the name or null if not found.
     */
    fun signedCertificate(serial: CordaX500Name) : X509Certificate?

    fun pendingCertificate(serial: CordaX500Name) : PKCS10CertificationRequest?

    /**
     * Get the root certificate as configured in the Spring properties
     */
    fun rootCertificate() : X509Certificate

    /**
     * Get the Doorman certificate as configured in the Spring properties
     */
    fun intermediateCertificate() : X509Certificate

    /**
     * Get the Network Map Certificate as configured in the Spring properties
     */
    fun networkMapCertificate() : X509Certificate

    /**
     * Sign the networkMap with the network map key
     */
    fun signNetworkMap(networkMap: NetworkMap): SignedNetworkMap

    /**
     *
     * Sign the network map params with the network map key
     */
    fun signNetworkParams(networkParams: NetworkParameters): SignedNetworkParameters

    /**
     * Validate the current setup, returns causes if configuration is incorrect
     */
    fun validate() : List<String>

    /**
     * clear all signed nodes and pending requests. Needed for testing purposes
     */
    fun clearAll()

    fun pendingSignRequests(): List<SignRequest>

    fun signedCertificates(): List<SignRequest>
}
