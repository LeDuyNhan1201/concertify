package org.tma.intern.common.security.impl;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.tma.intern.common.security.IdentityContext;

import java.util.List;

@RequestScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdentityContextImpl implements IdentityContext {

    SecurityIdentity securityIdentity;

    JsonWebToken jwt;

    public String getPrincipleName() {
        return !securityIdentity.isAnonymous() ? securityIdentity.getPrincipal().getName() : "Anonymous";
    }

    @Override
    public String getAccessToken() {
        return !securityIdentity.isAnonymous()
            ? securityIdentity.getCredential(AccessTokenCredential.class).getToken() : null;
    }

    @Override
    public List<String> getRoles() {
        return securityIdentity.getRoles().stream().toList();
    }

    @Override
    public String getClaim(String key) {
        return !securityIdentity.isAnonymous() ? jwt.getClaim(key) : null;
    }

}
