package org.tma.intern.auth.service.implementation;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.auth.dto.request.GroupRequest;
import org.tma.intern.auth.service.GroupService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.identity.IdentityGroup;

@ApplicationScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KeycloakGroupService extends BaseService implements GroupService {

    IdentityAdminClient keycloakAdminClient;

    @Override
    public Uni<String> createGroup(GroupRequest.GroupCreation request) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(keycloakAdminClient.createGroup(request.groupType(), request.region()))
            .action(Action.CREATE)
            .entityType(IdentityGroup.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

}
