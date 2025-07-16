package org.tma.intern.common.type.identity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResourceType {

    CONCERT("Concerts", "concerts"),
    BOOKING("Bookings", "bookings")
    ;

    public final String name;
    public final String type;

}
