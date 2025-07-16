package org.tma.intern.auth.data;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.common.type.Region;

import java.util.List;

public interface IdentityAdminClient {

    Multi<String> getRoles();

    Uni<String> createGroup(IdentityGroup group, Region region);

    Uni<IdentityUser> getUserByEmail(String email);

    Uni<String> createUser(IdentityUser entity, IdentityGroup group, Region region);

    Uni<String> deleteUser(String id);

    Multi<String> createUsers(List<IdentityUser> entities, IdentityGroup groups, Region region);

}
