package org.tma.intern.auth.data;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.dto.Region;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdentityUser {

    String id;

    String email;

    String password;

    Region region;

    List<String> roles;

}
