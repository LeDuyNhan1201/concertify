package org.tma.intern.common.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Region {

    US("us", "en"),
    VN("vn", "vi"),
    FR("fr", "fr"),
    ;

    public final String country;
    public final String tag;

}
