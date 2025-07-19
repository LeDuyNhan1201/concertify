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
import org.tma.intern.common.type.Region;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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
    public <T> T getClaim(String key) {
        return !securityIdentity.isAnonymous() ? jwt.getClaim(key) : null;
    }

    @Override
    public boolean hasAllRoles(List<String> roles) {
        return securityIdentity.getRoles().containsAll(roles);
    }

    @Override
    public boolean hasAnyRole(List<String> roles) {
        return securityIdentity.getRoles().stream().anyMatch(roles::contains);
    }

    @Override
    public Region getRegion() {
        HashSet<String> groups = getClaim("groups");
        return extractCountryCode(groups.stream().toList());
    }

    private Region extractCountryCode(List<String> groupPaths) {
        for (String path : groupPaths) {
            if (path.startsWith("/global/")) {
                String[] parts = path.split("/");
                if (parts.length > 2) {
                    return Region.valueOf(parts[2].toUpperCase(Locale.ROOT)); // phần tử sau "/global/"
                }
            }
        }
        return null; // hoặc throw exception nếu không tìm thấy
    }

}
