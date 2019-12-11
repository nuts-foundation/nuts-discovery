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

package nl.nuts.discovery

import net.corda.core.CordaOID
import net.corda.core.crypto.Crypto
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.PartyAndCertificate
import net.corda.core.internal.CertRole
import net.corda.core.node.NodeInfo
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.ContentSignerBuilder
import net.corda.nodeapi.internal.crypto.X509Utilities
import net.corda.testing.internal.signWith
import nl.nuts.discovery.service.CertificateAndKeyService
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.security.KeyPair
import java.security.cert.CertPath

interface TestUtils {
    companion object {
        fun createCertificateRequest(subject: CordaX500Name): PKCS10CertificationRequest {
            val keyPair = Crypto.generateKeyPair(Crypto.RSA_SHA256)
            return createCertificateRequest(subject, keyPair)
        }

        fun createCertificateRequest(subject: CordaX500Name, keyPair : KeyPair): PKCS10CertificationRequest {
            val signatureScheme = Crypto.RSA_SHA256
            val email = "a@b.com"

            val signer = ContentSignerBuilder.build(signatureScheme, keyPair.private, Crypto.findProvider(signatureScheme.providerName))
            return JcaPKCS10CertificationRequestBuilder(subject.x500Principal, keyPair.public)
                    .addAttribute(BCStyle.E, DERUTF8String(email))
                    .addAttribute(ASN1ObjectIdentifier(CordaOID.X509_EXTENSION_CORDA_ROLE), CertRole.NODE_CA)
                    .build(signer)
        }

        fun createNodeInfo(certPath: CertPath) : NodeInfo {
            return NodeInfo(
                    listOf(NetworkHostAndPort.parse("nuts.test:8080")),
                    listOf(PartyAndCertificate(certPath)),
                    3,
                    1)
        }

        fun subjectToSignedNodeInfo(service: CertificateAndKeyService, subject: CordaX500Name) : SignedNodeInfo {
            val nodeKeyPair = Crypto.generateKeyPair(Crypto.RSA_SHA256)
            val identityKeyPair = Crypto.generateKeyPair(Crypto.RSA_SHA256)
            // needs to generate well known identity certificate

            val req = createCertificateRequest(subject, nodeKeyPair)
            val nodeCertificate = service.signCertificate(req)

            val identityCertificate = X509Utilities.createCertificate(CertificateType.LEGAL_IDENTITY, nodeCertificate, nodeKeyPair, subject.x500Principal, identityKeyPair.public)

            val certPath = X509Utilities.buildCertPath(identityCertificate, nodeCertificate, service.intermediateCertificate(), service.rootCertificate())
            val nodeInfo = TestUtils.createNodeInfo(certPath)

            return nodeInfo.signWith(listOf(identityKeyPair.private))
        }
    }
}