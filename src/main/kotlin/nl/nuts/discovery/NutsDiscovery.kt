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

import net.corda.core.serialization.internal.SerializationEnvironment
import net.corda.core.serialization.internal.nodeSerializationEnv
import net.corda.node.serialization.amqp.AMQPServerSerializationScheme
import net.corda.serialization.internal.AMQP_P2P_CONTEXT
import net.corda.serialization.internal.SerializationFactoryImpl
import nl.nuts.discovery.service.CertificateAndKeyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import javax.annotation.PostConstruct

/**
 * Mainclass for booting the app
 */
@EnableConfigurationProperties
@SpringBootApplication
class NutsDiscovery {

    @Autowired
    lateinit var certificateAndKeyService: CertificateAndKeyService

    /**
     * Contains Corda magic to enable Object serialization
     */
    @PostConstruct
    fun init() {
        val problems = certificateAndKeyService.validate()

        if (!problems.isEmpty()) {

            println("Nuts Discovery failed to start, found ${problems.size} problems:")

            problems.forEach{ p ->
                println(p)
            }

            System.exit(1)
        }

        if (nodeSerializationEnv == null) {
            nodeSerializationEnv = SerializationEnvironment.with(
                    SerializationFactoryImpl().apply {
                        registerScheme(AMQPServerSerializationScheme(emptyList()))
                    },
                    AMQP_P2P_CONTEXT
            )
        }
    }
}

fun main(args: Array<String>) {
    try {
        runApplication<NutsDiscovery>(*args)
    } catch(e: Exception) {
        e.printStackTrace()
    }
}
