#!/bin/sh

# download corda.jar
curl https://repo1.maven.org/maven2/net/corda/corda/4.3/corda-4.3.jar --output corda.jar

# generate keys
cd .. && ./generate_keys.sh

# copy truststore
cd standalone-node && mkdir certificates && cp ../keys/truststore.jks certificates/network-root-truststore.jks