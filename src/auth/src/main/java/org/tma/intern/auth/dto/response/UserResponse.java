package org.tma.intern.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.type.Region;

import java.util.List;

public interface UserResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Details {

        String id;

        String email;

        Region region;

        List<String> roles;

    }

}
