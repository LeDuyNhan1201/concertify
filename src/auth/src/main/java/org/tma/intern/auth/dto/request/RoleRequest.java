package org.tma.intern.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import org.tma.intern.common.type.identity.ClientScope;
import org.tma.intern.common.type.identity.IdentityClient;

public interface RoleRequest {

    record ClientRoleInfo(

        @NotNull(message = "Validation.Empty")
        IdentityClient clientType,

        @NotNull(message = "Validation.Empty")
        ClientScope clientScope,

        @NotNull(message = "Validation.Empty")
        String realmRoleName

    ) {
    }

    ;

}
