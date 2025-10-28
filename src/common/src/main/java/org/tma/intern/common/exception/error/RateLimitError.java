package org.tma.intern.common.exception.error;

public final class RateLimitError {

    public final SubError ToMany = new SubError("rate-limit/to-many", "RateLimit.ToMany");

}
