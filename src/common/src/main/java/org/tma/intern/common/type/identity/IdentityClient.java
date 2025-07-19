package org.tma.intern.common.type.identity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IdentityClient {

    AUTH("auth-service", "auth"),
    CONCERT("concert-service", "concert"),
    BOOKING("booking-service", "booking")
    ;

    public final String id;
    public final String rolePrefix;

}
