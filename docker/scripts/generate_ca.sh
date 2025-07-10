#!/bin/bash

set -euo pipefail

CA_DIR="../certs/ca"
CA_KEY="$CA_DIR/ca.key"
CA_CERT="$CA_DIR/ca.crt"

mkdir -p "$CA_DIR"

# Step 1: Generate CA private key
echo "🔐 Generating Root CA private key..."
openssl genrsa -out "$CA_KEY" 4096

# Step 2: Generate Root CA certificate (self-signed)
echo "📜 Generating self-signed Root CA certificate..."
openssl req -x509 -new -nodes -key "$CA_KEY" \
    -sha256 -days 3650 \
    -subj "/C=VN/ST=BinhTan/L=HCM/O=TMA/OU=Dev/CN=LDNhanCA" \
    -out "$CA_CERT"

chmod 777 "$CA_KEY" "$CA_CERT"

echo -e "\n🎉 Root CA generated successfully:"
echo " - CA Key:  $CA_KEY"
echo " - CA Cert: $CA_CERT"
