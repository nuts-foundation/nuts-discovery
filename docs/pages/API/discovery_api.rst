.. _nuts-discovery-api:

******************
Nuts discovery Api
******************

The *Nuts Discovery Service* consists of 2 api's: the network map and certificate api's. The certificate api is only used in the initial setup phase of a Corda node. To publish the node details with an electronic signature and retrieve the signed certificate from the *Nuts Discovery Service*. The network map api is used to retrieve details about all the nodes that are connected to the network. These api's are called by the Corda node and should not be called by any other logic.

Certificate API
===============

The *Certificate API* is part of the Corda node but it's seems to be lacking documentation. After reverse engineering, 2 requests could be identified. The initation process for a Corda node can be started with:
::

    java -jar corda.jar initial-registration -p <password>

where the given password is the password of the Corda root truststore (for Java, *changeit* is used a lot). More info on how to start and configure a node can be found in :ref:`node setup <nuts-discovery-setup>`.

When started, the Corda node will create a couple of keystore as seen `here <https://docs.corda.net/permissioning.html#certificate-hierarchy>`_. The node will send a certificate request to the configured *Doorman* url followed by /certificate. The return value will be an identifier which the node will use to poll the service. When the service has signed the request, the certificate is then downloaded and put in the keystore. Below are the details of the two services.

.. http:post:: /certificate

   Receiving endpoint for a certificate request (CSR). The body must be in PKCS10 byte format. The response is a plain text response with an identifier which can be used in the GET call.

   :reqheader Platform-Version: the Corda platform version (4)
   :reqheader Client-Version: todo
   :reqheader Private-Network-Map: todo

   :statuscode 200: no error, body is a plain text string identifier
   :statuscode 400: wrong format certificate signing request

.. http:get:: /(request_id)

   Retrieve the signed certificate which was requesting using the POST method. `request_id` is the returned identifier from the POST request. The body contains a zip file with 3 files: **cordaclientca.cer**, **cordaintermediateca.cer** and **cordarootca.cer** (in this order). Each file is the ASN.1 DER encoding of a X.509 certificate.

   :resheader Content-Disposition: (attachment; filename="certificates.zip")

   :statuscode 200: Certificate request has been signed, the body consists of a zip file with a list of certificates.
   :statuscode 400: something went wrong.
   :statuscode 404: Unknown identifier or request hasn't been signed yet.

Network map API
===============

As described on https://docs.corda.net/network-map.html

Fetching specific (old) network parameters and acking a new set of parameters is currently beyond the scope of the current supported modes.

.. http:post:: /network-map/publish

   This endpoint is called by a node during its startup phase. The node will send a serialized **SignedNodeInfo** object which has been signed with the node private key. The *Nuts Discovery Service* will store the **SecureHash**, the unsigned **NodeInfo** and the list of **signatures**. The SecureHash will function as an index within the NetworkMap. The signatures are checked by other nodes when they download the NodeInfo for this node.

todo: move
   **Parsing logic**:

   .. sourcecode:: kotlin

      fun acceptNodeInfo(@RequestBody input: ByteArray) : ResponseEntity<ByteArray> {
        try {
          val signedNodeInfo = ByteArrayInputStream(input).readObject<SignedNodeInfo>()
          val hash = signedNodeInfo.raw.hash
          val nodeInfo = signedNodeInfo.verified()
          val signatures = signedNodeInfo.signatures

          nutsRegistrarService.publishNode(hash, nodeInfo, signatures)

        } catch (e: Exception) {
          logger.error(e.message, e)
        }

        return ResponseEntity.ok("".toByteArray())
      }

   :resheader Content-Type: application/octet-stream

   :statuscode 200: Ok with empty body
   :statuscode 500: Something went wrong

.. http:post:: /network-map/ack-parameters

   *currently not implemented*

.. http:get:: /network-map

   Returns the currently global active NetworkMap. All nodes that have been published and accepted by the *Nuts Discovery Service* will be in the output list. The output only consists of the node hashes and the hash of the current active network parameters. The call returns a **SignedNetworkMap** object signed with the NetworkMap private key. The cache control header is used by the node for a refresh interval.

   :resheader Content-Type: application/octet-stream
   :resheader Cache-Control: max-age=[X seconds]

   :statuscode 200: Ok with serialized NetworkMap
   :statuscode 500: Something went wrong

.. http:get:: /network-map/(var)

   *currently not implemented*

.. http:get:: /network-map/node-info/(hash)

   Fetch the specific **NodeInfo** indicated by `hash`. The NodeInfo will be the same as published by the node. The *Nuts Discovery Service* can't manipulate this since the signatures correspond to the private key of the node. The result will be a **SignedNodeInfo** object. The original NodeInfo and signatures from the publish api are used.

   :resheader Content-Type: application/octet-stream

   :statuscode 200: Ok with SignedNodeInfo object
   :statuscode 404: Unknown hash

.. http:get:: /network-map/network-parameters/(hash)

   Fetch the specific **NetworkParameters** indicated by `hash`. Currently this only returns the currently active NetworkParameters. The NetworkParameters contain:

   - minimum platform version
   - a list of notaries
   - maximum message size in bytes
   - maximum transaction size in bytes
   - modified timestamp
   - epoch (unknown what this does)
   - a whitelist of approved contract implementation

   :resheader Content-Type: application/octet-stream

   :statuscode 200: Ok with SignedNetworkParameters object
   :statuscode 404: Unknown hash