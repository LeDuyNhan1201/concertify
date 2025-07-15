package org.tma.intern.common.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BookingStatus {

    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELED("Canceled")
    ;

    public final String value;

}
