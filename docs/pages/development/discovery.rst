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

Two docker files are available in ``docker/``, the ``Dockerfile-dev`` is targeted at running a Nuts node at your laptop/workstation. It uses the keys and certificates from ``docker/keys``. This is also the place where you can find the ``truststore.jks`` needed for the corda nodes. Both images are build automatically and are hosted on docker hub. The dev-image is tagged as ``latest-dev``.

To build locally

.. code-block:: shell

    docker build . -f docker/Dockerfile
    docker build . -f docker/Dockerfile-dev

To run the latest dev image:

.. code-block:: shell

    docker run -it -p 8080:8080 --network=nuts --name=discovery --rm nutsfoundation/nuts-discovery:latest-dev

If you haven't created a docker network yet, create one to to able to connect the containers together:

.. code-block:: shell

    docker network create -d bridge nuts

When running the latest non-dev image, make sure all paths from ``docker/application.properties`` are mounted from a volume.

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