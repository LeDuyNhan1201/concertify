package org.tma.intern.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.type.Region;

import java.util.List;

public class UserResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Detail {

        String id;

        String email;

        Region region;

        List<String> roles;

    }

}
