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

@Path("/v1/concerts")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Concerts for Customers", description = "Concert for Customers operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerConcertsResourceV1 extends BaseResource {

    ConcertService concertService;

    @RolesAllowed(ROLE_VIEW_CONCERT) // Only customers
    @GET
    @Path("/{id}")
    @NoCache
    @Operation(summary = "Get concert details", description = "API to get details of a concert by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = ConcertResponse.PreviewWithSeats.class)))
    public Uni<RestResponse<CommonResponse<ConcertResponse.PreviewWithSeats>>> details(@PathParam("id") String id) {
        // Check region
        hasRole(IdentityRole.CUSTOMER);
        return concertService.preview(id).onItem().transform(concert ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<ConcertResponse.PreviewWithSeats>builder().data(concert).build()).build()
        );
    }

    @RolesAllowed(ROLE_VIEW_CONCERT) // Only customers
    @POST
    @Path("")
    @NoCache
    @Operation(summary = "Get concerts page", description = "API to search concerts.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public Uni<RestResponse<PageResponse<ConcertResponse.Preview>>> search(
        ConcertRequest.SearchQuery query,
        @QueryParam("offset") int offset,
        @QueryParam("limit") int limit
    ) {
        hasRole(IdentityRole.CUSTOMER);
        return concertService.search(query, offset, limit, false)
            .map(page -> RestResponse.ResponseBuilder.ok(page).build());
    }

}
