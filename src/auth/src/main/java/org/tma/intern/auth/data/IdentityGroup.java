package org.tma.intern.auth.data;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IdentityGroup {

    CUSTOMERS("/customers"),
    ORGANIZERS("/organizers"),
    ;

    public final String value;

}
