package org.tma.intern.common.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Action {

    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    READ("read")
    ;

    public final String message;

}
