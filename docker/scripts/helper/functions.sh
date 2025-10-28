#!/bin/bash

create_client_files() {
  echo "Creating client files"
  rm -rf ../kafka/configs/*
  cat ../templates/server.template | envsubst > ../kafka/configs/server.properties
  cat ../templates/client.template | envsubst > ../kafka/configs/client.properties
}

create_env_file() {
    # Clean old content before overwrite
    : > ../.env
    : > ../kafka/creds.txt

    echo POSTGRES_USER=$POSTGRES_USER >> ../.env
    echo POSTGRES_PASSWORD=$POSTGRES_PASSWORD >> ../.env

    echo MONGODB_USERNAME=$MONGODB_USERNAME >> ../.env
    echo MONGODB_PASSWORD=$MONGODB_PASSWORD >> ../.env

    echo KC_BOOTSTRAP_ADMIN_USERNAME=$KC_BOOTSTRAP_ADMIN_USERNAME >> ../.env
    echo KC_BOOTSTRAP_ADMIN_PASSWORD=$KC_BOOTSTRAP_ADMIN_PASSWORD >> ../.env

    echo CERT_SECRET=$CERT_SECRET >> ../.env
    echo $CERT_SECRET >> ../kafka/creds.txt

    echo BROKER_HEAP=$BROKER_HEAP >> ../.env
    echo SCHEMA_HEAP=$SCHEMA_HEAP >> ../.env
    echo SSL_CIPHER_SUITES=$SSL_CIPHER_SUITES >> ../.env
    echo KAFKA_OAUTH_LIB_VERSION=$KAFKA_OAUTH_LIB_VERSION >> ../.env
    echo NIMBUS_JWT_LIB_VERSION=$NIMBUS_JWT_LIB_VERSION >> ../.env
    echo PROMETHEUS_JAVAAGENT_VERSION=$PROMETHEUS_JAVAAGENT_VERSION >> ../.env

    echo KAFKA_IDP_TOKEN_ENDPOINT=$KAFKA_IDP_TOKEN_ENDPOINT >> ../.env
    echo KAFKA_IDP_JWKS_ENDPOINT=$KAFKA_IDP_JWKS_ENDPOINT >> ../.env
    echo KAFKA_IDP_EXPECTED_ISSUER=$KAFKA_IDP_EXPECTED_ISSUER >> ../.env
    echo KAFKA_IDP_AUTH_ENDPOINT=$KAFKA_IDP_AUTH_ENDPOINT >> ../.env
    echo KAFKA_IDP_DEVICE_AUTH_ENDPOINT=$KAFKA_IDP_DEVICE_AUTH_ENDPOINT >> ../.env
    echo KAFKA_SUB_CLAIM_NAME=$KAFKA_SUB_CLAIM_NAME >> ../.env
    echo KAFKA_GROUP_CLAIM_NAME=$KAFKA_GROUP_CLAIM_NAME >> ../.env
    echo KAFKA_EXPECTED_AUDIENCE=$KAFKA_EXPECTED_AUDIENCE >> ../.env

    # Client configurations
    echo KAFKA_SUPERUSER_CLIENT_ID=$KAFKA_SUPERUSER_CLIENT_ID >> ../.env
    echo KAFKA_SUPERUSER_CLIENT_SECRET=$KAFKA_SUPERUSER_CLIENT_SECRET >> ../.env

    echo KAFKA_SR_CLIENT_ID=$KAFKA_SR_CLIENT_ID >> ../.env
    echo KAFKA_SR_CLIENT_SECRET=$KAFKA_SR_CLIENT_SECRET >> ../.env

    echo KAFKA_C3_CLIENT_ID=$KAFKA_C3_CLIENT_ID >> ../.env
    echo KAFKA_C3_CLIENT_SECRET=$KAFKA_C3_CLIENT_SECRET >> ../.env

    echo KAFKA_CLIENT_ID=$KAFKA_CLIENT_ID >> ../.env
    echo KAFKA_CLIENT_SECRET=$KAFKA_CLIENT_SECRET >> ../.env

    echo KAFKA_SSO_CLIENT_ID=$KAFKA_SSO_CLIENT_ID >> ../.env
    echo KAFKA_SSO_CLIENT_SECRET=$KAFKA_SSO_CLIENT_SECRET >> ../.env

    echo KAFKA_SSO_SUPER_USER_GROUP=$KAFKA_SSO_SUPER_USER_GROUP >> ../.env
    echo KAFKA_SSO_USER_GROUP=$KAFKA_SSO_USER_GROUP >> ../.env

    echo AUTH_CLIENT_ID=$AUTH_CLIENT_ID >> ../.env
    echo AUTH_CLIENT_SECRET=$AUTH_CLIENT_SECRET >> ../.env

    echo CONCERT_CLIENT_ID=$CONCERT_CLIENT_ID >> ../.env
    echo CONCERT_CLIENT_SECRET=$CONCERT_CLIENT_SECRET >> ../.env

    echo BOOKING_CLIENT_ID=$BOOKING_CLIENT_ID >> ../.env
    echo BOOKING_CLIENT_SECRET=$BOOKING_CLIENT_SECRET >> ../.env

}

