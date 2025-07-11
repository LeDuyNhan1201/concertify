package org.tma.intern.common.exception.handler;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        return Response.status(401)
                .header("WWW-Authenticate", "Basic realm=\"Quarkus\"")
                .entity("Unauthorized")
                .build();
    }
}