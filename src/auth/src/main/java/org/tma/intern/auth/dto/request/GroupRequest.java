package org.tma.intern.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

public interface GroupRequest {

    record GroupCreation(

        @NotNull(message = "Validation.Empty")
        IdentityGroup groupType,

        @NotNull(message = "Validation.Empty")
        Region region

    ) {
    }

    ;

}
