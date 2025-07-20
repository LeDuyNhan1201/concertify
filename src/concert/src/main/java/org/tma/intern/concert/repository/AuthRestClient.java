package org.tma.intern.concert.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

import java.util.List;

@RequestScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthRestClient {

    WebClient webClient;

    ObjectMapper objectMapper = new ObjectMapper();

    OidcClient oidcClient;

    @NonFinal
    @ConfigProperty(name = "rest-client.auth-service.url")
    String AUTH_SERVICE_URL;

    public Uni<String> getAccessToken() {
        return oidcClient.getTokens().onFailure().transform(error ->
            new HttpException(AppError.ACTION_FAILED, Response.Status.UNAUTHORIZED, error, "Get", "user ids")
        ).map(Tokens::getAccessToken);
    }

    public Uni<List<String>> getAllUserEmail(IdentityGroup group, Region region) {
        return getAccessToken().flatMap(accessToken -> webClient.getAbs(AUTH_SERVICE_URL + "/v1/internal/users")
            .addQueryParam("group", group.type)
            .addQueryParam("region", region.country)
            .putHeader("Content-Type", "application/json") // sửa lại đúng MIME type
            .putHeader("Authorization", "Bearer " + accessToken)
            .send()
            .onFailure().invoke(err -> log.error("Request failed", err))
            .map(response -> {
                if (response.statusCode() != 200) {
                    log.error("Fetch failed: {} - {}", response.statusCode(), response.statusMessage());
                    throw new HttpException(AppError.ACTION_FAILED, Response.Status.fromStatusCode(response.statusCode()), null, "Get", "user ids");
                }

                try {
                    CommonResponse<List<String>> myResponse = objectMapper.readValue(response.bodyAsString(), new TypeReference<>() {
                    });
                    return myResponse.getData();
                } catch (Exception e) {
                    log.error("Parse error", e);
                    throw new HttpException(AppError.ACTION_FAILED, Response.Status.INTERNAL_SERVER_ERROR, e, "Parse", "user ids response");
                }
            }));
    }


}
