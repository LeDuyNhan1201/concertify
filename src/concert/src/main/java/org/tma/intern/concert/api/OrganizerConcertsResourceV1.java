package org.tma.intern.concert.api;

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
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestResponse;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.type.identity.IdentityRole;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;
import org.tma.intern.concert.service.ConcertService;

import java.util.List;

@Path("/v1/organizer/concerts")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Concerts", description = "Concert operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizerConcertsResourceV1 extends BaseResource {

    ConcertService concertService;

    static final String CREATE_ROLE = "concert:create";
    static final String UPDATE_ROLE = "concert:edit";
    static final String VIEW_ROLE = "concert:view";

    @RolesAllowed("global_admin")
    @PATCH
    @Path("/{id}/approve")
    @Operation(summary = "Approve concert", description = "API to approve an exist concert by id.")
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> approve(@PathParam("id") String id) {
        return concertService.approve(id).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Approve", "concert"))
                .data(id).build()
            ).build());

    }

    @RolesAllowed(CREATE_ROLE) // Only organizers
    @POST
    @Path("")
    @Operation(summary = "Create concert", description = "API to create a new concert.")
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> create(ConcertRequest.Info info) {
        hasOnlyRole(IdentityRole.ORGANIZER);
        return concertService.create(info).onItem().transform(id ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.CREATED, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Create", "concert"))
                .data(id).build()
            ).build());
    }

    @RolesAllowed(UPDATE_ROLE) // Only organizers
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update concert", description = "API to update an exist concert by id.")
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> update(@PathParam("id") String id, ConcertRequest.Info info) {
        // Check owner of concert
        hasOnlyRole(IdentityRole.ORGANIZER);
        return concertService.update(id, info).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Update", "concert"))
                .data(id).build()
            ).build());

    }

    @RolesAllowed(UPDATE_ROLE) // Only organizers
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete concert", description = "API to soft delete a concert by id.")
    @APIResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> softDelete(@PathParam("id") String id) {
        // Check owner of concert
        hasOnlyRole(IdentityRole.ORGANIZER);
        return concertService.softDelete(id).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Soft delete", "concert"))
                .data(id).build()
            ).build());
    }

    @RolesAllowed(VIEW_ROLE) // Only organizers
    @GET
    @Path("/{id}")
    @NoCache
    @Operation(summary = "Get my concert details", description = "API to get details of a concert of mine by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = ConcertResponse.DetailsWithSeats.class)))
    public Uni<RestResponse<CommonResponse<ConcertResponse.DetailsWithSeats>>> details(@PathParam("id") String id) {
        // Check owner of concert
        hasOnlyRole(IdentityRole.ORGANIZER);
        return concertService.details(id).onItem().transform(concert ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<ConcertResponse.DetailsWithSeats>builder().data(concert).build()).build());
    }

    @RolesAllowed(VIEW_ROLE) // Only organizers
    @GET
    @Path("")
    @NoCache
    @Operation(summary = "Search my concerts", description = "API to search my concerts.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public Uni<RestResponse<PageResponse<ConcertResponse.Preview>>> myConcerts(
        @QueryParam("offset") int offset,
        @QueryParam("limit") int limit) {
        hasOnlyRole(IdentityRole.ORGANIZER);
        return concertService.myConcerts(offset, limit).map(page -> RestResponse.ResponseBuilder.ok(page).build());
    }

    @GET
    @Path("/seed/{count}")
    @NoCache
    @Operation(summary = "Seed concerts", description = "API to seed concerts with count.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = Integer.class)))
    public Uni<RestResponse<CommonResponse<List<String>>>> seed(int count) {
        return concertService.seedData(count).map(ids -> RestResponse.ok(CommonResponse.<List<String>>builder().data(ids).build()));
    }

}
