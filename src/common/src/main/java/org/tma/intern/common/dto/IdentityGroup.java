package org.tma.intern.common.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IdentityGroup {

    CUSTOMERS("customers"),
    ORGANIZERS("organizers"),
    ADMINISTRATORS("administrators"),
    ;

    public final String type;

}
