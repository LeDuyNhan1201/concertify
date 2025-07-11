package org.tma.intern.common.exception;

import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HttpException extends RuntimeException {

    public HttpException(AppError error, Response.Status httpStatus, Throwable throwable, String... moreInfo) {
        super(error.getMessage(), throwable);
        this.httpStatus = httpStatus;
        this.moreInfo = moreInfo;
        this.error = error;
    }

    Response.Status httpStatus;
    AppError error;
    Object[] moreInfo;

}