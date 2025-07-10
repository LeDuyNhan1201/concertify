# IDP configurations
export IDP_TOKEN_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/token
export IDP_JWKS_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/certs
export IDP_EXPECTED_ISSUER=https://keycloak:8443/realms/kafka
export IDP_AUTH_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/auth
export IDP_DEVICE_AUTH_ENDPOINT=https://keycloak:8443/realms/kafka/protocol/openid-connect/auth/device
export SUB_CLAIM_NAME=sub
export GROUP_CLAIM_NAME=groups
export EXPECTED_AUDIENCE=account

export CERT_SECRET=120103
export BROKER_HEAP=1G
export SCHEMA_HEAP=512M
export SSL_CIPHER_SUITES=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256

# Client configurations
export APP_GROUP_NAME='/clients'

export POSTGRES_USER=ldnhan
export POSTGRES_PASSWORD=123

export MONGODB_USERNAME=ldnhan
export MONGODB_PASSWORD=123

export KC_BOOTSTRAP_ADMIN_USERNAME=ldnhan
export KC_BOOTSTRAP_ADMIN_PASSWORD=123

export SUPERUSER_CLIENT_ID=kafka
export SUPERUSER_CLIENT_SECRET=kafka-secret

export SR_CLIENT_ID=schema-registry
export SR_CLIENT_SECRET=schema-registry-secret

export C3_CLIENT_ID=control-center
export C3_CLIENT_SECRET=control-center-secret

export CLIENT_APP_ID=quarkus
export CLIENT_APP_SECRET=quarkus-secret

export SSO_CLIENT_ID=control-center-sso
export SSO_CLIENT_SECRET=control-center-sso-secret

export SSO_SUPER_USER_GROUP=sso-users
export SSO_USER_GROUP=users

