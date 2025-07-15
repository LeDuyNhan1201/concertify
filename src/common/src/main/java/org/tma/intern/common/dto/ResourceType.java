package org.tma.intern.common.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResourceType {

    CONCERT("Concerts", "concerts"),
    BOOKING("Bookings", "bookings")
    ;

    public final String name;
    public final String type;

}
