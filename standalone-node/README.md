# Test the discovery service using a real Corda node

This is a setup to run a Corda node locally (without docker) and register it using the discovery service. 
The discovery service has to be run using gradle, from within your IDE or by executing the jar.

## Prerequisites

- Java 8
- Java keytool
- generated keys in keys/ 

## Directory contents

- corda.jar - the main corda executable
- node.conf - Corda node config
- certificates/network-root-truststore.jks - contains the root certificate, must be the same as keys/truststore.jks

## Usage

You may have to `chmod +x *.sh` to run the scripts.

### Setup

Run once after `git clone` or `clean`. When prompted for password, enter `changeit`. Enter `yes` when prompted for trust question.

```
./setup.sh
```

To generate keys, download Corda, etc.

### Register

Creates a cert request and send it to the discovery service. The discovery service must be running for this. Afterwards the node will start polling for a certificate.

```
./register.sh
```

### Run

Starts the corda node, which is a notary.

```
./run.sh
```

### Clean

To get rid of all the stuff.

```
./clean.sh
```