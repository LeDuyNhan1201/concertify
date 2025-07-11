package org.tma.intern.auth.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "oauth2")
public interface OAuth2Config {
    String clientId();
    String clientSecret();
    String tokenEndpoint();
    String scope();
}
