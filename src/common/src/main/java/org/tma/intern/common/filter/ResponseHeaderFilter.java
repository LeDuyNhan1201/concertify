package org.tma.intern.common.filter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.apache.http.HttpHeaders;

import java.util.Locale;
import java.util.Objects;

@Provider
public class ResponseHeaderFilter implements ContainerResponseFilter {

    @Context
    HttpServerRequest req;

    @Context
    HttpServerResponse resp;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String locale = req.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        resp.putHeader(HttpHeaders.CONTENT_LANGUAGE, Objects.requireNonNullElseGet(locale, Locale.ENGLISH::toString));
    }

}
