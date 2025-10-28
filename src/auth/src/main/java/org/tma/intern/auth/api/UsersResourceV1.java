package org.tma.intern.auth.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import org.tma.intern.auth.dto.request.UserRequest;
import org.tma.intern.auth.dto.response.UserResponse;
import org.tma.intern.auth.service.UserService;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

@Path("/v1/users")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Users", description = "User operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsersResourceV1 extends BaseResource {

    UserService userService;

    @POST
    @Path("/sign-up")
    @Operation(summary = "Sign up user", description = "Sign up a new user")
    @APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> signUp(@Valid UserRequest.Registration body) {
        return userService.signUp(body).onItem().transform(userId ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.CREATED, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Sign up", "user"))
                .data(userId).build()
            ).build());
    }

    @RolesAllowed(ROLE_GLOBAL_ADMIN)
    @POST
    @Path("")
    @Operation(summary = "Create user", description = "Create a new user")
    @APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> create(UserRequest.Creation body) {
        return userService.createUser(body).onItem().transform(userId ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.CREATED, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Create", "user"))
                .data(userId).build()
            ).build());
    }

    @RolesAllowed(ROLE_GLOBAL_ADMIN)
    @GET
    @Path("/{email}")
    @NoCache
    @Operation(summary = "Get user details", description = "API to get details of user by email")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = UserResponse.Details.class)))
    public Uni<RestResponse<CommonResponse<UserResponse.Details>>> details(@PathParam("email") String email) {
        return userService.getUserByEmail(email).onItem().transform(user ->
            RestResponse.ResponseBuilder.ok(
                CommonResponse.<UserResponse.Details>builder().data(user).build()
            ).build());
    }

    @RolesAllowed(ROLE_GLOBAL_ADMIN)
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete user", description = "API to get delete user by id")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> delete(@PathParam("id") String id) {
        return userService.delete(id).onItem().transform(userId ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.OK, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Delete", "user"))
                .data(userId).build()
            ).build());
    }

    @Authenticated
    @GET
    @Path("/me")
    @NoCache
    @Operation(summary = "Get current user", description = "API to get name of current user")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = UserResponse.Details.class)))
    public Uni<RestResponse<UserResponse.Details>> me() {
        return Uni.createFrom().item(() -> RestResponse.ResponseBuilder.ok(UserResponse.Details.builder()
            .id(identityContext.getClaim("sub"))
            .email(identityContext.getPrincipleName())
            .roles(identityContext.getRoles())
            .region(identityContext.getRegion())
            .build()
        ).build());
    }

    @RolesAllowed(ROLE_GLOBAL_ADMIN)
    @GET
    @Path("/seed")
    @NoCache
    @Operation(summary = "Seed users", description = "API to seed user with [Role]:user.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> seed(
        @QueryParam("count") int count,
        @QueryParam("groupType") String group,
        @QueryParam("region") String region
    ) {
        return userService.seedUsers(count,
            IdentityGroup.valueOf(group.toUpperCase()),
            Region.valueOf(region.toUpperCase())
        ).map(userIds -> Response.ok(userIds).build());
    }

}