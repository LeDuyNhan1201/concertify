package org.tma.intern.common.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SeatType {

    VIP("Vip", 120.0),
    STANDARD("Standard", 60.0),
    ;

    public final String value;

    public final double price;

}
