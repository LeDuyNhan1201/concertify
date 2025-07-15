#!/bin/bash

if [ "$EUID" -ne 0 ]; then
  echo "Please run as root"
  exit 1
fi

docker compose down -v

echo "Removing certs..."

rm -f ../kafka/broker1/certs/*
rm -f ../kafka/schema-registry1/certs/*
rm -f ../keycloak/certs/*
rm -rf ../../src/auth/src/main/resources/certs/*

echo "Done."