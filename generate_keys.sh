#!/usr/bin/env bash

OUTDIR="./keys/"
CONFDIR="./setup/corda/"
CONFDIR2="./setup/nuts/"

if ! hash openssl 2>/dev/null; then
  echo "openssl not found"
  exit 1
fi

if ! hash keytool 2>/dev/null; then
  echo "keytool not found"
  exit 1
fi

mkdir -p keys
rm keys/*

echo "Generating Corda root key and certificate..."
if ! openssl req -new -nodes -keyout ${OUTDIR}root.key -config ${CONFDIR}root.conf -days 1825 -out ${OUTDIR}root.csr
then
  echo "unable to generate root.csr"
  exit 1
fi

if ! openssl x509 -req -days 1825 -in ${OUTDIR}root.csr -signkey ${OUTDIR}root.key -out ${OUTDIR}root.crt -extfile ${CONFDIR}root.conf
then
  echo "unable to generate root.crt"
  exit 1
fi

echo "Generating Doorman key and certificate..."
if ! openssl req -new -nodes -keyout ${OUTDIR}doorman.key -config ${CONFDIR}doorman.conf -days 1825 -out ${OUTDIR}doorman.csr
then
  echo "unable to generate doorman.csr"
fi

if ! openssl x509 -req -days 1825 -in ${OUTDIR}doorman.csr -CA ${OUTDIR}root.crt -CAkey ${OUTDIR}root.key -CAcreateserial -out ${OUTDIR}doorman.crt -extfile ${CONFDIR}doorman.conf
then
  echo "unable to generate doorman.crt"
  exit 1
fi

echo "Generating NetworkMap key and certificate..."
if ! openssl req -new -nodes -keyout ${OUTDIR}network_map.key -config ${CONFDIR}network_map.conf -days 1825 -out ${OUTDIR}network_map.csr
then
  echo "unable to generate network_map.csr"
  exit 1
fi

if ! openssl x509 -req -days 1825 -in ${OUTDIR}network_map.csr -CA ${OUTDIR}root.crt -CAkey ${OUTDIR}root.key -CAcreateserial -out ${OUTDIR}network_map.crt -extfile ${CONFDIR}network_map.conf
then
  echo "unable to generate network_map.crt"
  exit 1
fi

echo "Creating root truststore..."
if ! keytool -import -file ${OUTDIR}root.crt -alias cordarootca -keystore ${OUTDIR}truststore.jks
then
  echo "could not create root truststore"
  exit 1
fi

echo "Generating Nuts root key and certificate..."
if ! openssl ecparam -out ${OUTDIR}nuts_root.key -name secp384r1 -genkey
then
  echo "unable to generate nuts_root.key"
  exit 1
fi
if ! openssl req -new -key ${OUTDIR}nuts_root.key -config ${CONFDIR2}root.conf -days 1825 -out ${OUTDIR}nuts_root.csr
then
  echo "unable to generate nuts_root.csr"
  exit 1
fi
if ! openssl x509 -req -days 1825 -in ${OUTDIR}nuts_root.csr -signkey ${OUTDIR}nuts_root.key -out ${OUTDIR}nuts_root.crt -extfile ${CONFDIR2}root.conf
then
  echo "unable to generate nuts_root.crt"
  exit 1
fi

echo "Generating Nuts CA key and certificate..."
if ! openssl ecparam -out ${OUTDIR}nuts_ca.key -name secp384r1 -genkey
then
  echo "unable to generate nuts_ca.key"
  exit 1
fi
if ! openssl req -new -nodes -key ${OUTDIR}nuts_ca.key -config ${CONFDIR2}nuts_ca.conf -days 1825 -out ${OUTDIR}nuts_ca.csr
then
  echo "unable to generate nuts_ca.csr"
fi

if ! openssl x509 -req -days 1825 -in ${OUTDIR}nuts_ca.csr -CA ${OUTDIR}nuts_root.crt -CAkey ${OUTDIR}nuts_root.key -CAcreateserial -out ${OUTDIR}nuts_ca.crt -extfile ${CONFDIR2}nuts_ca.conf
then
  echo "unable to generate nuts_ca.crt"
  exit 1
fi

echo "done"
