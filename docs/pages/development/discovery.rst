.. _nuts-discovery-development:

Nuts discovery service development
##################################

.. marker-for-readme

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