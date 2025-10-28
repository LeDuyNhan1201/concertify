package org.tma.intern.auth.api;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestResponse;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

import java.util.List;

@Path("/v1/internal")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Users for Internal services", description = "User for Internal services operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalResourceV1 extends BaseResource {

    IdentityAdminClient keycloakAdminClient;

    @RolesAllowed({"concert-service", "booking-service"})
    @GET
    @Path("/users")
    @NoCache
    @Operation(summary = "Get list user email", description = "API to get list user email by groupType")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    public Uni<RestResponse<CommonResponse<List<String>>>> getEmailsByGroup(
        @QueryParam("groupType") String group,
        @QueryParam("region") String region
    ) {
        return keycloakAdminClient.getAllUserEmailByGroup(
            IdentityGroup.valueOf(group.toUpperCase()), Region.valueOf(region.toUpperCase())
        ).onItem().transform(userEmails -> RestResponse.ResponseBuilder.ok(
            CommonResponse.<List<String>>builder().data(userEmails).build()
        ).build());
    }

}