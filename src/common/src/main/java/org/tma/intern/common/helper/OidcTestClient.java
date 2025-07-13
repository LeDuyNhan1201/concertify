package org.tma.intern.common.helper;

import io.quarkus.test.keycloak.client.KeycloakTestClient;

public class OidcTestClient {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    public String getAccessToken(String user) {
        return keycloakClient.getAccessToken(user, "123", "web-app");
    }

}
