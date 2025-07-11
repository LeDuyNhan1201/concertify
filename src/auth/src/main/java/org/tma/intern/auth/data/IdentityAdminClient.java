package org.tma.intern.auth.data;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

public interface IdentityAdminClient {

    Multi<String> getRoles();

    Uni<IdentityUser> getUserByEmail(String email);

    Uni<String> createUser(IdentityUser entity, IdentityGroup... groups);

    Multi<String> createUsers(List<IdentityUser> entities, IdentityGroup... groups);

    Uni<String> deleteUser(String id);

    Uni<Map<String, String>> getTokens(String username, String password);

}
