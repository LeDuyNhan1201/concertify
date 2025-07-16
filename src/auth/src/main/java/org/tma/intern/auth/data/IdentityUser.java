package org.tma.intern.auth.data;

import lombok.*;
import lombok.experimental.FieldDefaults;

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

    String firstName;

    String lastName;

}
