package org.tma.intern.common.type.identity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ClientScope {

    VIEW("view"),
    READ("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    ;

    public final String value;

}
