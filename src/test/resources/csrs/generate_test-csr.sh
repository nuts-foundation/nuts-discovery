#!/usr/bin/env bash

openssl req -new -nodes -keyout test.key -config test_csr.cnf -out test.csr
openssl req -new -nodes -keyout missing_oid.key -config missing_oid_csr.cnf -out missing_oid.csr
