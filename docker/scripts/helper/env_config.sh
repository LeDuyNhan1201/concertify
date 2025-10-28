export POSTGRES_USER=ldnhan
export POSTGRES_PASSWORD=123

export MONGODB_USERNAME=ldnhan
export MONGODB_PASSWORD=123

export KC_BOOTSTRAP_ADMIN_USERNAME=ldnhan
export KC_BOOTSTRAP_ADMIN_PASSWORD=123

export CERT_SECRET=120103
export BROKER_HEAP=1G
export SCHEMA_HEAP=512M
export SSL_CIPHER_SUITES=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
export KAFKA_OAUTH_LIB_VERSION=0.15.1
export NIMBUS_JWT_LIB_VERSION=9.37.2
export PROMETHEUS_JAVAAGENT_VERSION=1.3.0

# IDP configurations
export KAFKA_IDP_URL=https://keycloak:8443/
export KAFKA_IDP_TOKEN_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/token
export KAFKA_IDP_JWKS_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/certs
export KAFKA_IDP_EXPECTED_ISSUER=https://keycloak:8443/realms/kafka
export KAFKA_IDP_AUTH_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/auth
export KAFKA_IDP_DEVICE_AUTH_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/auth/device
export KAFKA_SUB_CLAIM_NAME=sub
export KAFKA_GROUP_CLAIM_NAME=groups
export KAFKA_EXPECTED_AUDIENCE=account

# Client configurations
export KAFKA_SUPERUSER_CLIENT_ID=kafka
export KAFKA_SUPERUSER_CLIENT_SECRET=kafka-secret

export KAFKA_SR_CLIENT_ID=schema-registry
export KAFKA_SR_CLIENT_SECRET=schema-registry-secret

export KAFKA_C3_CLIENT_ID=control-center
export KAFKA_C3_CLIENT_SECRET=control-center-secret

export KAFKA_CLIENT_ID=quarkus
export KAFKA_CLIENT_SECRET=quarkus-secret

export KAFKA_SSO_CLIENT_ID=control-center-sso
export KAFKA_SSO_CLIENT_SECRET=control-center-sso-secret

export KAFKA_SSO_SUPER_USER_GROUP=sso-users
export KAFKA_SSO_USER_GROUP=users

export AUTH_CLIENT_ID=auth-service
export AUTH_CLIENT_SECRET=X4Y6nlgqf7dX6ajQ449gRRu9fF0gfBw2

export CONCERT_CLIENT_ID=concert-service
export CONCERT_CLIENT_SECRET=ggj3eeGCjmgFsk8Yob5l3PiJ4hieDyod

export BOOKING_CLIENT_ID=booking-service
export BOOKING_CLIENT_SECRET=emGYC5XPSmeWUl2hfDbmlnqcCU500bac

