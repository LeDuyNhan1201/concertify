package org.tma.intern.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.tma.intern.common.type.identity.ClientScope;
import org.tma.intern.common.type.identity.IdentityClient;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.common.type.Region;

public class UserRequest {

    public record Registration(
        @Email(message = "Validation.Email")
        String email,
        @NotBlank(message = "Validation.Empty")
        @NotNull(message = "Validation.Empty")
        @Size(message = "Validation.Size", min = 3, max = 20)
        String password,
        String firstName,
        @NotBlank(message = "Validation.Empty")
        @NotNull(message = "Validation.Empty")
        @Size(message = "Validation.Size", min = 1, max = 20)
        String lastName
    ) {
    }
    ;

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

    public record ClientRoleScope(
        IdentityClient client,
        ClientScope scope,
        String realmRoleName
    ) {
    }
    ;

}
