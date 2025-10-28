package org.tma.intern.common.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.tma.intern.common.helper.TimeHelper;

import java.text.MessageFormat;
import java.time.Instant;

@Provider
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseLoggingFilter implements ContainerResponseFilter {

    @Context
    HttpServerRequest request;

    @Context
    HttpServerResponse response;

    final ObjectMapper mapper = initMapper();

    public static final String KEY_REQUEST_ID = "requestId";

    public static final String KEY_REQUEST_STARTED_AT = "requestStartedAt";

    public static final String KEY_REQUEST_EXECUTION_TIME = "requestExecutionTime";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Instant startedAt = null;
        try {
            Object contextStartedAt = requestContext.getProperty(KEY_REQUEST_STARTED_AT);
            if (contextStartedAt instanceof Instant) {
                startedAt = (Instant) contextStartedAt;
            }

            StringBuilder responseInfo = new StringBuilder();
            responseInfo.append(
                MessageFormat.format("{0} {1}\n", request.version(), response.getStatusCode())
            );
            response.headers().forEach(header -> responseInfo.append(
                    MessageFormat.format("{0}: {1}\n", header.getKey(), header.getValue())
                )
            );

            if (responseContext.getEntity() != null
                && MediaType.APPLICATION_JSON.equals(responseContext.getHeaderString(HttpHeaders.CONTENT_TYPE))
            ) {
                StringBuilder body = new StringBuilder(encode(responseContext.getEntity()));
                int maxLength = 300;
                if (body.length() > maxLength) {
                    body.delete(maxLength, body.length());
                    body.append("...more...}");
                }
                responseInfo.append(body);
            }

            if (startedAt != null) {
                double requestExecutionTime = TimeHelper.sinceMs(startedAt);
                addRequestExecutionTime(requestExecutionTime);
                log.info("Response took {}: {}\n", TimeHelper.toMsFormat(requestExecutionTime), responseInfo);
            }

        } catch (Exception e) {
            log.error("Response logging filter failed, caused by: {}", e.getMessage());

        } finally {
            removeRequestExecutionTime();
            removeRequestId();
        }
    }

    public static void addRequestExecutionTime(double executionTime) {
        MDC.put(KEY_REQUEST_EXECUTION_TIME, executionTime);
    }

    public static void removeRequestId() {
        MDC.remove(KEY_REQUEST_ID);
    }

    public static void removeRequestExecutionTime() {
        MDC.remove(KEY_REQUEST_EXECUTION_TIME);
    }

    private ObjectMapper initMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private String encode(@NonNull Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "Fail to parse JSON: " + e.getMessage();
        }
    }

}
