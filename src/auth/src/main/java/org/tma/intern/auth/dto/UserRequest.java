package org.tma.intern.auth.dto;

import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.common.type.Region;

public class UserRequest {

    public record Creation(
        String email,
        String password,
        String firstName,
        String lastName,
        IdentityGroup group,
        Region region
    ) {
    }

    ;

    public record GroupCreation(
        IdentityGroup group,
        Region region
    ) {
    }

    ;

}
