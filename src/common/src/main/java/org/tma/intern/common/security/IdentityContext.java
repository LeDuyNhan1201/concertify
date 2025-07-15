package org.tma.intern.common.security;

import java.util.List;

public interface IdentityContext {

    String getPrincipleName();

    String getAccessToken();

    List<String> getRoles();

    <T> T getClaim(String key);

}
