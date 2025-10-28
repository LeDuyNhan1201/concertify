package org.tma.intern.auth.service.implementation;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.auth.dto.request.RoleRequest;
import org.tma.intern.auth.service.RoleService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.identity.IdentityGroup;

@ApplicationScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KeycloakRoleService extends BaseService implements RoleService {

    IdentityAdminClient keycloakAdminClient;

    @Override
    public Uni<String> assignClientRole(RoleRequest.ClientRoleInfo request) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(
                keycloakAdminClient.assignClientRole(
                    request.clientType(),
                    request.clientScope(),
                    request.realmRoleName()
                )
            )
            .action(Action.CREATE)
            .entityType(IdentityGroup.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

}
