package org.tma.intern.auth.api;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
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
import org.jboss.resteasy.reactive.RestResponse;
import org.tma.intern.auth.dto.request.RoleRequest;
import org.tma.intern.auth.service.RoleService;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;

@Path("/v1/roles")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Roles", description = "Role operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolesResourceV1 extends BaseResource {

    RoleService roleService;

    @RolesAllowed(ROLE_GLOBAL_ADMIN)
    @PUT
    @Path("")
    @Operation(summary = "Assign clientType role", description = "Assign clientType role to Realm role")
    @APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    public Uni<RestResponse<CommonResponse<String>>> assignClientRole(RoleRequest.ClientRoleInfo body) {
        return roleService.assignClientRole(body).onItem().transform(clientRoleId ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.CREATED, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Assign", "clientType role"))
                .data(clientRoleId).build()
            ).build());
    }

}