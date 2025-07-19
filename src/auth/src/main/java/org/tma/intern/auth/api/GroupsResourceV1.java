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
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.service.UserService;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;

@Path("/v1/groups")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Groups", description = "Group operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupsResourceV1 extends BaseResource {

    UserService userService;

    @RolesAllowed(ROLE_GLOBAL_ADMIN)
    @POST
    @Path("")
    @Operation(summary = "Create group", description = "Create a new group")
    @APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    public Uni<RestResponse<CommonResponse<String>>> createGroup(UserRequest.GroupCreation body) {
        return userService.createGroup(body).onItem().transform(groupId ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.CREATED, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Create", "group"))
                .data(groupId).build()
            ).build());
    }

}