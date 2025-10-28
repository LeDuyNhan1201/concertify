package org.tma.intern.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.smallrye.jwt.runtime.auth.JWTAuthMechanism;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.exception.AppError;

import java.util.Set;

@Alternative
@Priority(1)
@ApplicationScoped
@Slf4j
public class AuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    JWTAuthMechanism delegate;

    @Inject
    JWTAuthContextInfo jwtAuthContextInfo;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
        IdentityProviderManager identityProviderManager) {
        String headerName = jwtAuthContextInfo.getTokenHeader();
        String token = extractTokenFromBearerHeader(context.request().headers().get(headerName));

        // TODO: Execute additional token logic checking
        log.info("Access token: {}", token);

        return delegate.authenticate(context, identityProviderManager);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        CommonResponse<?> errorResponse = CommonResponse.builder()
            .code(AppError.Invalid.Token.getCode())
            .message("Unauthorized - CustomAuth required")
            .build();
        String json = "{\"error\":\"serialization failed\"}";

        try {
            json = objectMapper.writeValueAsString(errorResponse);

        } catch (JsonProcessingException exception) {
            log.error("Cannot stringify json caused by: {}", exception.getMessage());
        }

        context.response()
            .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
            .putHeader(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .putHeader(HttpHeaderNames.WWW_AUTHENTICATE, "CustomAuth")
            .end(json);

        return Uni.createFrom().nullItem();
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return delegate.getCredentialTypes();
    }

    private static String extractTokenFromBearerHeader(String token) {
        if (token == null) {
            return null;
        }

        return token.replace("Bearer ", "");
    }

}
