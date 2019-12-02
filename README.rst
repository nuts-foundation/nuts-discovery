nuts-discovery
##############

Discovery service by the Nuts foundation for bootstrapping the network

.. image:: https://travis-ci.org/nuts-foundation/nuts-discovery.svg?branch=master
    :target: https://travis-ci.org/nuts-foundation/nuts-discovery

.. image:: https://api.codacy.com/project/badge/Grade/cd7e8a20fd474ba1b5b5539dc68ffa3b
    :target: https://www.codacy.com/manual/nuts-foundation/nuts-discovery?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nuts-foundation/nuts-discovery&amp;utm_campaign=Badge_Grade

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

Two docker files are available in ``docker/``, the ``Dockerfile-dev`` is targeted at running a Nuts node at your laptop/workstation. The dev-image is tagged as ``latest-dev``.

To build locally

.. code-block:: shell

    docker build . -f docker/Dockerfile
    docker build . -f docker/Dockerfile-dev

Checkout :ref:`nuts-network-local-development-docker` for setting up a complete environment with ``docker-compose``.

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

Generate root key and certificate
---------------------------------

Run the ``generate_keys.sh`` script to create a ``keys`` folder with all the needed keys and certificates.

.. sourcecode:: shell

  ./generate_keys.sh

