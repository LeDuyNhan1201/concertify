package org.tma.intern.common.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.AuthenticationFailedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.exception.AppError;

@ApplicationScoped
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AuthenticationFailedExceptionHandler {

    @Inject
    ObjectMapper objectMapper;

    public void init(@Observes Router router) {
        router.route().failureHandler(event -> {
            if (event.failure() instanceof AuthenticationFailedException exception) {
                CommonResponse<?> errorResponse = CommonResponse.builder()
                    .code(AppError.Invalid.Token.getCode())
                    .message(exception.getMessage())
                    .build();
                String json = "{\"error\":\"serialization failed\"}";

                try {
                    json = objectMapper.writeValueAsString(errorResponse);

                } catch (JsonProcessingException jsonProcessingException) {
                    log.error("Cannot stringify json caused by: {}", jsonProcessingException.getMessage());
                }

                JsonObject responseBody = new JsonObject(json);

                event.response()
                    .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                    .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON)
                    .putHeader(HttpHeaderNames.WWW_AUTHENTICATE.toString(), "AuthHandler")
                    .end(responseBody.encode());

            } else {
                event.next();
            }
        });
    }
}