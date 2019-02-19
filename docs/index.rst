.. _nuts-discovery:

Nuts Discovery
==============

The *Nuts Discovery Service* is the Nuts implementation of the Network map service described by *Corda*. The *Corda* specific documentation can be found at https://docs.corda.net/network-map.html. The reason for implementing the network map as a service and not just distributing node information via other means is that this greatly simplifies development, puts the control of the root CA at the right place and creates a bridge to the *Nuts registry*. When a node registers with the discovery service, the service can also add the node to the registry. This will enable node administrators to link care providers to their Nuts node entry in the registry.

Corda often speaks of the *Doorman*. This is the *service* that is responsible for approving nodes, eg: signing certificate requests. The *Doorman* uses the intermediate CA to sign Node CA's. Right now, Nuts combines the *Doorman* service and the *NetworkMap* service in the *Nuts discovery Service*.

Back to main documentation: :ref:`nuts-documentation`

.. toctree::
    :maxdepth: 2
    :caption: Contents:
    :glob:

    pages/*
