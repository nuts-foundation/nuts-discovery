nuts-discovery
##############

Discovery service by the Nuts foundation for bootstrapping the network

.. image:: https://circleci.com/gh/nuts-foundation/nuts-discovery.svg?style=svg
    :target: https://circleci.com/gh/nuts-foundation/nuts-discovery
    :alt: Build Status

.. image:: https://api.codeclimate.com/v1/badges/6d39940d517f06989299/maintainability
   :target: https://codeclimate.com/github/nuts-foundation/nuts-discovery/maintainability
   :alt: Maintainability

.. image:: https://readthedocs.org/projects/nuts-discovery/badge/?version=latest
    :target: https://nuts-documentation.readthedocs.io/projects/nuts-discovery/en/latest/

.. image:: https://codecov.io/gh/nuts-foundation/nuts-discovery/branch/master/graph/badge.svg
    :target: https://codecov.io/gh/nuts-foundation/nuts-discovery

The discovery service is written in Kotlin and can be build by Gradle.

Dependencies
************

Since the discovery service depends on Corda, Java 1.8 is needed. For the Oracle sdk, this means that your version needs to be > 1.8 update 151.
This can give problems on several linux distro's. In that case use the latest OpenJDK sdk 1.8.

The project is build with Gradle. A gradle wrapper is present in the project.

Generating code
***************

To generate the Api stubs based on the Open Api Spec:

.. code-block:: shell

    ./gradlew generateServerApiStub

Running tests
*************

Tests can be run by executing

.. code-block:: shell

    ./gradlew test

Building
********

An executable can be build by executing

.. code-block:: shell

    ./gradlew bootJar

Running
*******

The server can be started by executing

.. code-block:: shell

    ./gradlew bootRun

This requires some files to be present in the *keys* sub-directory. Check :ref:`nuts-discovery-configuration` on how to configure the keys.

Docker
******

A Dockerfile is provided. As default it will run with dev properties and keys. This can be overriden by mounting the right dirs:

.. code-block:: shell

    docker run -it \
        -v {{KEYS_DIR}}:/opt/nuts/discovery/keys \
        -v {{CONF_DIR}}:/opt/nuts/discovery/conf \
        -p 8080:8080 \
        nutsfoundation/nuts-discovery:latest


README
******

The readme is auto-generated from a template and uses the documentation to fill in the blanks.

.. code-block:: shell

    ./generate_readme.sh

Documentation
*************

To generate the documentation, you'll need python3, sphinx and a bunch of other stuff. See :ref:`nuts-documentation-development-documentation`
The documentation can be build by running

.. code-block:: shell

    /docs $ make html

The resulting html will be available from ``docs/_build/html/index.html``

Configuration
=============

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
nuts.discovery.flowHashes                                       Sha256 of jars for nl.nuts.consent.flow package (comma separated)
nuts.discovery.contractHashes                                   Sha256 of jars for nl.nuts.consent.contract package (comma separated)
nuts.discovery.autoAck                  false                   Automatically signs all signing requests
===================================     ====================    ================================================================================

These locations can be overriden by providing an alternative properties file with the following contents

.. sourcecode:: properties

    nuts.discovery.rootCertPath = keys/root.crt
    nuts.discovery.intermediateKeyPath = keys/doorman.key
    nuts.discovery.intermediateCertPath = keys/doorman.crt
    nuts.discovery.networkMapCertPath = keys/network_map.crt
    nuts.discovery.networkMapKeyPath = keys/network_map.key
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

