package org.tma.intern.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.common.type.Region;

public interface UserRequest {

    record Registration(

        @Email(message = "Validation.Email")
        String email,

        @NotBlank(message = "Validation.Empty")
        @NotNull(message = "Validation.Empty")
        @Size(message = "Validation.Size", min = 3, max = 20)
        String password,

        String firstName,

        @NotBlank(message = "Validation.Empty")
        @NotNull(message = "Validation.Empty")
        @Size(message = "Validation.Size", min = 1, max = 50)
        String lastName

    ) {
    }

    record Creation(

        @Email(message = "Validation.Email")
        String email,

        @NotBlank(message = "Validation.Empty")
        @NotNull(message = "Validation.Empty")
        @Size(message = "Validation.Size", min = 3, max = 20)
        String password,

        String firstName,

        @NotBlank(message = "Validation.Empty")
        @NotNull(message = "Validation.Empty")
        @Size(message = "Validation.Size", min = 3, max = 50)
        String lastName,

        @NotNull(message = "Validation.Empty")
        IdentityGroup groupType,

        @NotNull(message = "Validation.Empty")
        Region region

    ) {
    }

}
