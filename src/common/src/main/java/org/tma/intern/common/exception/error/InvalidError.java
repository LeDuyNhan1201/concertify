package org.tma.intern.common.exception.error;

public final class InvalidError {

    public final SubError Token = new SubError("invalid/token", "Invalid.Token");

    public final SubError Role = new SubError("invalid/role", "Invalid.Role");

    public final SubError Region = new SubError("invalid/region", "Invalid.Region");

}
