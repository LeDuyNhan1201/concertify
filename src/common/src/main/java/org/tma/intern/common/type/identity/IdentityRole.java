package org.tma.intern.common.type.identity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IdentityRole {

    CUSTOMER("customer"),
    ORGANIZER("organizer"),
    ADMINISTRATOR("administrator"),
    ;

    public final String prefix;

}
