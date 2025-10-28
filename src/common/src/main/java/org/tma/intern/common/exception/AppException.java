package org.tma.intern.common.exception;

import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.exception.error.SubError;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppException extends RuntimeException {

    public AppException(SubError error, Throwable throwable, Response.Status httpStatus, String... moreInfo) {
        super(error.getMessageKey(), throwable);
        this.httpStatus = httpStatus;
        this.moreInfo = moreInfo;
        this.error = error;
    }

    Response.Status httpStatus;
    SubError error;
    Object[] moreInfo;

}