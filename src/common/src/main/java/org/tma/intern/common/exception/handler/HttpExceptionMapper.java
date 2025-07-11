package org.tma.intern.common.exception.handler;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.locale.LocaleProvider;

@Provider
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class HttpExceptionMapper implements ExceptionMapper<HttpException> {

    LocaleProvider locale;

    @Override
    public Response toResponse(HttpException exception) {
        AppError error = exception.getError();
        CommonResponse<String> response = CommonResponse.<String>builder()
            .code(error.getCode())
            .message((exception.getMoreInfo() != null)
                ? locale.getMessage(error.getMessage(), exception.getMoreInfo())
                : locale.getMessage(error.getMessage()))
            .data((exception.getCause() == null) ? exception.getCause().getMessage() : null)
            .build();

        return Response.status(exception.getHttpStatus()).entity(response).build();
    }

}
