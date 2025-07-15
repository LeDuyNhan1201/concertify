package org.tma.intern.common.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IdentityRole {

    CUSTOMER("customer"),
    ORGANIZER("organizer"),
    ADMINISTRATOR("administrator"),
    ;

    public final String value;

}
