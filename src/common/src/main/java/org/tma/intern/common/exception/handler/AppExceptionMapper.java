package org.tma.intern.common.exception.handler;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.exception.AppException;
import org.tma.intern.common.exception.error.SubError;

@Provider
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppExceptionMapper implements ExceptionMapper<AppException> {

    @Override
    public Response toResponse(AppException exception) {
        SubError error = exception.getError().args(exception.getMoreInfo());
        CommonResponse<String> response = CommonResponse.<String>builder()
            .code(error.getCode())
            .message(error.getMessage())
            .data((exception.getCause() != null) ? exception.getCause().getMessage() : null)
            .build();

        return Response.status(exception.getHttpStatus()).entity(response).build();
    }

}
