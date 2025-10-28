package org.tma.intern.auth.data;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.tma.intern.common.type.identity.ClientScope;
import org.tma.intern.common.type.identity.IdentityClient;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.common.type.Region;

import java.util.List;

public interface IdentityAdminClient {

    Uni<List<String>> getAllUserEmailByGroup(IdentityGroup group, Region region);

    Uni<String> createGroup(IdentityGroup group, Region region);

    Uni<IdentityUser> getUserByEmail(String email);

    Uni<String> createUserIntoGroup(IdentityUser entity, IdentityGroup group, Region region);

    Uni<String> deleteUserById(String id);

    Multi<String> createUsers(List<IdentityUser> entities, IdentityGroup groups, Region region);

    Uni<String> assignClientRole(IdentityClient client, ClientScope scope, String realmRoleName);

}
