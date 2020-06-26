.. _nuts-discovery-configuration:

Nuts discovery configuration
****************************

.. marker-for-readme

Before the *Nuts Discovery Service* can be started a few keys and certificates need to be generated. All OpenSSL commands use config files for the correct generation of certificates and keys. Windows scripts are currently lacking.

By default it'll try to find the following keys at the given location. All files are in PEM format

===================================     ====================    ================================================================================
Key                                     Default                 Description
===================================     ====================    ================================================================================
nuts.discovery.cordaRootCertPath        keys/root.crt           Corda root certificate path
nuts.discovery.intermediateKeyPath      keys/doorman.key        Corda doorman key path, used to sign node csr's
nuts.discovery.intermediateCertPath     keys/doorman.crt        Corda doorman certificate path
nuts.discovery.networkMapCertPath       keys/network_map.crt    Corda network map certificate path
nuts.discovery.networkMapKeyPath        keys/network_map.key    Corda network map key path, used to sign network parameters and nodeinfo objects
nuts.discovery.nutsRootCertPath         keys/nuts_root.crt      Nuts root certificate path, trust anchor for p2p connections between nodes
nuts.discovery.nutsCAKeyPath            keys/nuts_ca.key        Key path for Nuts CA, used to sign vendor certificates
nuts.discovery.nutsCACertPath           keys/nuts_ca.crt        Cert path for Nuts CA, used to sign vendor certificates
nuts.discovery.flowHashes                                       Sha256 of jars for nl.nuts.consent.flow package (comma separated)
nuts.discovery.contractHashes                                   Sha256 of jars for nl.nuts.consent.contract package (comma separated)
nuts.discovery.autoAck                  false                   Automatically signs all signing requests
===================================     ====================    ================================================================================

These locations can be overriden by providing an alternative properties file with the following contents

.. sourcecode:: properties

    nuts.discovery.cordaRootCertPath = keys/root.crt
    nuts.discovery.intermediateKeyPath = keys/doorman.key
    nuts.discovery.intermediateCertPath = keys/doorman.crt
    nuts.discovery.networkMapCertPath = keys/network_map.crt
    nuts.discovery.networkMapKeyPath = keys/network_map.key
    nuts.discovery.nutsRootCertPath = keys/nuts_root.crt
    nuts.discovery.nutsCAKeyPath = keys/nuts_ca.key
    nuts.discovery.nutsCACertPath = keys/nuts_ca.crt
    nuts.discovery.contractHashes = 6ACDE387C0DF227A6C4ED77407B58E9103C2EA1A66796CE37BC497931F4E1631
    nuts.discovery.flowHashes = 5f60201e5f4e698300f3baf94dad1517a1314b4f406fd90610a78d798ffe972d
    nuts.discovery.autoAck = true

The alternative config file can be passed to the executable by param like this

.. sourcecode:: shell

    java -jar nuts-discovery.jar --spring.config.location=file:./custom.properties

Individual properties can also be overriden by passing them via the command-line

.. sourcecode:: shell

    java -jar nuts-discovery.jar --nuts.discovery.networkMapKeyPath=keys/network_map.key

Or by using environment variables

.. sourcecode:: shell

    NUTS_DISCOVERY_NETWORK_MAP_KEY_PATH=keys/network_map.key java -jar nuts-discovery.jar

Besides the keys and certificates it's also possible to change the ``server.port`` property.

Key generation
==============

Generate root key and certificate
---------------------------------

Run the ``generate_keys.sh`` script to create a ``keys`` folder with all the needed keys and certificates.

.. sourcecode:: shell

  ./generate_keys.sh

Deployment with Helm
====================

Installation
-----------

In the following examples we use the `development` namespace. The values.yaml currently contains values for development.

.. sourcecode:: shell

  helm install --debug --name discovery --namespace development charts/nuts-discovery -f charts/nuts-discovery/values.yaml

Upgrading
---------

.. sourcecode:: shell

  helm upgrade discovery -f charts/nuts-discovery/values.yaml charts/nuts-discovery --namespace development --recreate-pods
