package org.tma.intern.common.type.identity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ClientScope {

    VIEW("view"),
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),

    SEAT_UPDATE("seat:update"),

    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    ;

    public final String value;

}
