.. _nuts-discovery-configuration:

Nuts discovery configuration
****************************

.. marker-for-readme

Before the *Nuts Discovery Service* can be started a few keys and certificates need to be generated. All OpenSSL commands use config files for the correct generation of certificates and keys. Windows scripts are currently lacking.

By default it'll try to find the following keys at the given location. All files are in PEM format

===================================     ====================    ================================================================================
Key                                     Default                 Description
===================================     ====================    ================================================================================
nuts.discovery.rootCertPath             keys/root.crt           Corda root certificate path
nuts.discovery.intermediateKeyPath      keys/doorman.key        Corda doorman key path, used to sign node csr's
nuts.discovery.intermediateCertPath     keys/doorman.crt        Corda doorman certificate path
nuts.discovery.networkMapCertPath       keys/network_map.crt    Corda network map certificate path
nuts.discovery.networkMapKeyPath        keys/network_map.key    Corda network map key path, used to sign network parameters and nodeinfo objects
nuts.discovery.autoAck                  false                   Automatically signs all signing requests
===================================     ====================    ================================================================================

These locations can be overriden by providing an alternative properties file with the following contents

.. sourcecode:: properties

    nuts.discovery.rootCertPath = keys/root.crt
    nuts.discovery.intermediateKeyPath = keys/doorman.key
    nuts.discovery.intermediateCertPath = keys/doorman.crt
    nuts.discovery.networkMapCertPath = keys/network_map.crt
    nuts.discovery.networkMapKeyPath = keys/network_map.key
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
