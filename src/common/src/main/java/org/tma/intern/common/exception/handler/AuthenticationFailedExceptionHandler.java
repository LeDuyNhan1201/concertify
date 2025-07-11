package org.tma.intern.common.exception.handler;

import io.quarkus.security.AuthenticationFailedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.tma.intern.common.exception.AppError;

@ApplicationScoped
public class AuthenticationFailedExceptionHandler {

    public void init(@Observes Router router) {
        router.route().failureHandler(event -> {
            if (event.failure() instanceof AuthenticationFailedException) {
                JsonObject responseBody = new JsonObject()
                    .put("code", AppError.TOKEN_INVALID.getCode())
                    .put("message", AppError.TOKEN_INVALID.getMessage());

                event.response()
                    .setStatusCode(401)
                    .putHeader("Content-Type", "application/json")
                    .end(responseBody.encode());
            } else {
                event.next();
            }
        });
    }
}