package org.tma.intern.common.filter;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Provider
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestLoggingFilter implements ContainerRequestFilter {

    @Context
    HttpServerRequest request;

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    public static final String KEY_REQUEST_ID = "requestId";

    public static final String KEY_REQUEST_STARTED_AT = "requestStartedAt";

    public static final String KEY_REQUEST_EXECUTION_TIME = "requestExecutionTime";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            requestContext.setProperty(KEY_REQUEST_STARTED_AT, Instant.now());
            Object uuid = requestContext.getProperty(KEY_REQUEST_ID);
            if (uuid == null) {
                uuid = request.headers().get(HEADER_REQUEST_ID);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                }
                requestContext.setProperty(KEY_REQUEST_ID, uuid);
            }
            addRequestId(String.valueOf(uuid));

            StringBuilder requestInfo = new StringBuilder();
            requestInfo.append(
                MessageFormat.format("{0} {1} {2}\n", request.method(), request.path(), request.version())
            );
            request.headers().forEach(header -> requestInfo.append(
                MessageFormat.format("{0}: {1}\n", header.getKey(), header.getValue())
                )
            );

            if (MediaType.APPLICATION_JSON.equals(request.headers().get(HttpHeaders.CONTENT_TYPE))) {
                String body = getRequestBody(requestContext);
                if (body != null && !body.isEmpty()) {
                    requestInfo.append(body);
                }
            }
            log.info("Request info: {}", requestInfo);

        } catch (Exception e) {
            log.error("Request logging filter failed, caused by: {}", e.getMessage());
        }
    }

    private String getRequestBody(ContainerRequestContext context) {
        try {
            String body = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
            InputStream inputStream = IOUtils.toInputStream(body, StandardCharsets.UTF_8);
            context.setEntityStream(inputStream);

            return StringUtils.isNotBlank(body)
                ? body.lines().map(String::trim).collect(Collectors.joining())
                : StringUtils.EMPTY;

        } catch (IOException e) {
            return "Fail to parse request body: " + e.getMessage();
        }
    }

    public static void addRequestId(@NonNull String requestId) {
        MDC.put(KEY_REQUEST_ID, requestId);
    }

}
