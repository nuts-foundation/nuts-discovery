.. _nuts-discovery-configuration:

.. marker-for-readme

Nuts discovery configuration
****************************

Before the *Nuts Discovery Service* can be started a few keys and certificates need to be generated. All OpenSSL commands use config files for the correct generation of certificates and keys. Windows scripts are currently lacking.

By default it'll try to find the following keys at the given location. All files are in PEM format

===================================     ====================    ================================================================================
Key                                     Default                 Description
===================================     ====================    ================================================================================
nuts.discovery.rootCertPath             keys/root.crt           Corda root certificate path
nuts.discovery.intermediateKeyPath      keys/doorman.key        Corda doorman key path, used to sign node csr's
nuts.discovery.intermediateCertPath     keys/doorman.crt        Corda doorman certificate path
nuts.discovery.networkMapCertPath       keys/network_map.crt    Corda network map certificate path
nuts.discovery.networkMapKeyPath        keys/network_map.key    corda network map key path, used to sign network parameters and nodeinfo objects
===================================     ====================    ================================================================================

These locations can be overriden by providing an alternative properties file with the following contents

.. sourcecode:: properties

    nuts.discovery.rootCertPath = keys/root.crt
    nuts.discovery.intermediateKeyPath = keys/doorman.key
    nuts.discovery.intermediateCertPath = keys/doorman.crt
    nuts.discovery.networkMapCertPath = keys/network_map.crt
    nuts.discovery.networkMapKeyPath = keys/network_map.key

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

The following commands are run from ``setup/corda``.


Generate root key and certificate
---------------------------------

.. sourcecode:: shell

   openssl req -new -nodes -keyout root.key -config root.conf -days 1825 -out root.csr
   openssl x509 -req -days 1825 -in root.csr -signkey root.key -out root.crt -extfile root.conf


Generate Doorman key and certificate
------------------------------------

.. sourcecode:: shell

   openssl req -new -nodes -keyout doorman.key -config doorman.conf -days 1825 -out doorman.csr
   openssl x509 -req -days 1825 -in doorman.csr -CA root.crt -CAkey root.key -CAcreateserial -out doorman.crt -extfile doorman.conf


Generate NetworkMap key and certificate
---------------------------------------

.. sourcecode:: shell

   openssl req -new -nodes -keyout network_map.key -config network_map.conf -days 1825 -out network_map.csr
   openssl x509 -req -days 1825 -in network_map.csr -CA root.crt -CAkey root.key -CAcreateserial -out network_map.crt -extfile network_map.conf


Create root truststore
----------------------

The root truststore needs to be copied to each running node. You'll need the Java keytool for this. The default truststore password is used for now: *changeit*

.. sourcecode:: shell

   keytool -import -file root.crt -alias cordarootca -keystore truststore.jks