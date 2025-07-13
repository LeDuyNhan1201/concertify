package org.tma.intern.common.exception;

import lombok.Getter;

@Getter
public enum AppError {

    // Token Errors
    TOKEN_MISSING("common/token-missing", "Error.TokenMissing"),
    TOKEN_INVALID("common/token-invalid", "Error.TokenInvalid"),

    //Rate Limiting Errors
    TOO_MANY_REQUESTS("common/too-many-requests", "Error.TooManyRequests"),
    RATE_LIMIT_EXCEEDED("common/rate-limit-exceeded", "Error.RateLimitExceeded"),

    RESOURCE_NOT_FOUND("common/resource-not-found", "Error.ResourceNotFound"),
    ACTION_FAILED("common/action-failed", "Action.Fail"),
    AUTH_INFO_INVALID("auth/auth-info-invalid", "Error.AuthInfoInvalid"),
    REGION_INVALID("auth/region-invalid", "Error.RegionInvalid"),
    ;

    AppError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;
    private final String message;

}