.. _nuts-discovery-installation:

Nuts discovery service installation
###################################

Installation of the discovery service is as easy as running it from development (:ref:`nuts-discovery-development`). You can choose to create a runnable jar first

.. code-block:: shell

    ./gradlew bootJar

And then start it via

.. code-block::

    java -jar nuts-discovery-x.y.z.jar


Make sure you use a java 8 compatible JVM.