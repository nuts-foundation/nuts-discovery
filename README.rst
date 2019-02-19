**************
nuts-discovery
**************

Discovery service by the Nuts foundation for bootstrapping the network

.. inclusion-marker-for-contribution

Certificates
============

Before the *Nuts Discovery Service* can be started a few keys and certificates need to be generated. All OpenSSL commands use config files for the correct generation of certificates and keys. Windows scripts are currently lacking.

The following commands are run from setup/corda.


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

Configuration
=============

todo
