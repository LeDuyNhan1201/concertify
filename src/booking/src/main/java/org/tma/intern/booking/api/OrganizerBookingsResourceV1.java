package org.tma.intern.booking.api;

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
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.type.BookingStatus;
import org.tma.intern.common.type.identity.IdentityRole;

import java.util.List;

@Path("/v1/organizer/bookings")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Bookings for Organizer", description = "Booking for Organizer operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizerBookingsResourceV1 extends BaseResource {

    BookingService bookingService;

    @RolesAllowed("booking:update") // Only Organizer that owns the concert
    @PATCH
    @Path("/{id}/{status}")
    @Operation(summary = "Update booking", description = "API to update an exist booking by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> update(@PathParam("id") String id, @PathParam("status") BookingStatus status) {
        // Check owner of concert
        hasOnlyRole(IdentityRole.ORGANIZER);
        return bookingService.update(id, status).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Update", "booking"))
                .data(id).build()
            ).build());

    }

    @RolesAllowed("booking:view") // Only Organizer that owns the concert
    @GET
    @Path("/{id}")
    @NoCache
    @Operation(summary = "Get booking details", description = "API to get details of a booking by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = BookingResponse.Details.class)))
    public Uni<RestResponse<CommonResponse<BookingResponse.Preview>>> details(@PathParam("id") String id) {
        // Check owner of concert
        hasOnlyRole(IdentityRole.ORGANIZER);
        return bookingService.preview(id).onItem().transform(booking ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<BookingResponse.Preview>builder().data(booking).build()).build()
        );
    }

    @RolesAllowed("booking:view") // Only Organizer that owns the concerts
    @GET
    @Path("/{concertId}/concert")
    @NoCache
    @Operation(summary = "Search bookings", description = "API to search bookings.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public Uni<RestResponse<PageResponse<BookingResponse.Details>>> bookingsOfMyConcerts(
        @PathParam("concertId") String concertId,
        @QueryParam("index") int index,
        @QueryParam("limit") int limit) {
        // Check owner of concert
        hasOnlyRole(IdentityRole.ORGANIZER);
        return bookingService.bookingsOfMyConcerts(index, limit).map(page -> RestResponse.ResponseBuilder.ok(page).build());
    }

    @GET
    @Path("/seed/{count}")
    @NoCache
    @Operation(summary = "Seed bookings", description = "API to seed bookings with count.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = Integer.class)))
    public Uni<RestResponse<CommonResponse<List<String>>>> seed(int count) {
        return bookingService.seedData(count).map(ids -> RestResponse.ok(CommonResponse.<List<String>>builder().data(ids).build()));
    }

}
