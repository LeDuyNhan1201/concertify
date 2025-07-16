package org.tma.intern.common.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BookingStatus {

    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELED("Canceled")
    ;

    public final String value;

}
