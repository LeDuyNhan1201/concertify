#!/bin/bash
set -euo pipefail

export CA_COMMON_NAME="LDNhanCA"
export SUBJ_C="VN"
export SUBJ_ST="BinhTan"
export SUBJ_L="HCM"
export SUBJ_O="TMA"
export SUBJ_OU="Dev"

export CERTS_DIR="../certs"

mode=${1:-"dev"}

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

env_file="${DIR}/helper/env_config.sh"
echo "Processing $env_file"

source "$env_file"
source "${DIR}/helper/functions.sh"
source "${DIR}/helper/generate_certs.sh"

create_client_files
create_env_file
generate_root_ca

generate_keystore_and_truststore "keycloak" "keycloak"

generate_keystore_and_truststore "kafka1" "kafka1"
#generate_keystore_and_truststore "kafka2" "kafka2"
#generate_keystore_and_truststore "kafka3" "kafka3"

generate_keystore_and_truststore "schema-registry1" "schema1"

generate_keystore_and_truststore "auth-service" "auth"
generate_keystore_and_truststore "concert-service" "concert"
generate_keystore_and_truststore "booking-service" "booking"

cp $CERTS_DIR/auth-service/* ../../src/auth/src/main/resources/certs/
cp $CERTS_DIR/concert-service/* ../../src/concert/src/main/resources/certs/
cp $CERTS_DIR/booking-service/* ../../src/booking/src/main/resources/certs/

#docker compose -f docker-compose."${mode}".yml up -d