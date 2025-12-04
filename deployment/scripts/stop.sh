#!/bin/bash

mode=${1:-"dev"}

if [ "$EUID" -ne 0 ]; then
  echo "Please run as root"
  exit 1
fi

#docker compose -f docker-compose."${mode}".yml down -v

echo "Removing certs..."

rm -rf ../certs/*
rm -rf ../../src/auth/src/main/resources/certs/*
rm -rf ../../src/concert/src/main/resources/certs/*
rm -rf ../../src/booking/src/main/resources/certs/*

echo "Done."