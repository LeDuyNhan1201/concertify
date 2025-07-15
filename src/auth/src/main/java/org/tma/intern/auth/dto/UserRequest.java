package org.tma.intern.auth.dto;

import org.tma.intern.common.dto.IdentityGroup;
import org.tma.intern.common.dto.Region;

public class UserRequest {

    public record Creation(
            String email,
            String password,
            String firstName,
            String lastName,
            IdentityGroup group
    ){};

    public record GroupCreation(
        IdentityGroup group,
        Region region
    ){};

}
