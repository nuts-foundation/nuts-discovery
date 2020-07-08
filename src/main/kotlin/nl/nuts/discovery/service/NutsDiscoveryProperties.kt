package nl.nuts.discovery.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Class used to store the application configuration options.
 */
@Configuration
@ConfigurationProperties("nuts.discovery")
class NutsDiscoveryProperties {
    lateinit var cordaRootCertPath: String
    lateinit var intermediateCertPath: String
    lateinit var intermediateKeyPath: String
    lateinit var networkMapCertPath: String
    lateinit var networkMapKeyPath: String
    lateinit var nutsRootCertPath: String
    lateinit var nutsCACertPath: String
    lateinit var nutsCAKeyPath: String
    lateinit var contractHashes: String
    lateinit var flowHashes: String
    lateinit var certificateValidityInDays: Integer
    var autoAck: Boolean = false
}
