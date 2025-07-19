package org.tma.intern.common.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SeatType {

    VIP(120.0),
    STANDARD(60.0),
    ;

    public final double price;

}
