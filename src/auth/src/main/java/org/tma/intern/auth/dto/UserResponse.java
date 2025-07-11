package org.tma.intern.auth.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.auth.data.Region;

import java.util.List;

public class UserResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Details {

        String id;

        String email;

        Region region;

        List<String> roles;

    }

}
