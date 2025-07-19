package org.tma.intern.common.security;

import org.tma.intern.common.type.Region;

import java.util.List;

public interface IdentityContext {

    String getPrincipleName();

    String getAccessToken();

    List<String> getRoles();

    <T> T getClaim(String key);

    boolean hasAllRoles(List<String> roles);

    boolean hasAnyRole(List<String> roles);

    Region getRegion();

}
