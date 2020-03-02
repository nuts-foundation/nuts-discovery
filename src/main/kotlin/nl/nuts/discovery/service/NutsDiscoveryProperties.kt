package nl.nuts.discovery.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Class used to store the application configuration options.
 */
@Configuration
@ConfigurationProperties("nuts.discovery")
class NutsDiscoveryProperties {
    lateinit var rootCertPath: String
    lateinit var intermediateCertPath: String
    lateinit var intermediateKeyPath: String
    lateinit var networkMapCertPath: String
    lateinit var networkMapKeyPath: String
    var autoAck: Boolean = false
}
