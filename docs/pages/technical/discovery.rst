.. _nuts-discovery-technical:

Nuts consent discovery
######################

Keys and certificates
*********************

The figure at https://docs.corda.net/network-map.html shows the CA structure of Corda nodes. The *Nuts Discovery Service* represents the *security zone* in that figure. To startup the service, a few keys need to be generated and configured. More info on this can be found in :ref:`nuts-discovery-setup`. Only the root CA is configured at each node as trusted anchor. Both the Doorman and Network Map CA are trusted because they are signed by the root. The CertRole for each CA is important, to prevent mistakes configuration files for generating these certificates can be found in this repository.

.. note:: All mentioned keys and certificates are only used for the Corda part of the Nuts network. Communication between care providers use different certificates. This ensures that only the right parties can issue/revoke certificates based on the responsibilities they have. The *Nuts Discovery Service* can only determine who joins the network. It cannot access or manipulate any data. The root key should not be present on any production environments. It can even be controlled by a different part of the Nuts community to ensure decentralization of control.

Modes of operation
******************

The *Nuts Discovery Service* currently has only 1 mode of operation: local development. In the future it will support production and other modes.

Local development
=================

The local development mode is targeted at running a Nuts network locally. To prevent any manual configuration, the *Nuts Discovery Service* will auto-sign all certificate signing requests it receives. Check :ref:`nuts-discovery-setup` on how to start the service.