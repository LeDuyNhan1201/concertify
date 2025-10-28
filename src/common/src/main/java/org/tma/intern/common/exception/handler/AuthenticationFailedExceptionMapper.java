package org.tma.intern.common.exception.handler;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.AuthenticationFailedException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.exception.AppError;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        return Response.status(HttpResponseStatus.UNAUTHORIZED.code())
            .header(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON)
            .header(HttpHeaderNames.WWW_AUTHENTICATE.toString(), "AuthMapper")
            .entity(CommonResponse.builder()
                .code(AppError.Invalid.Token.getCode())
                .message(exception.getMessage())
                .build())
            .build();
    }

}