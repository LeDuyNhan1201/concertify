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
import org.tma.intern.booking.dto.request.BookingRequest;
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.type.identity.IdentityRole;

@Path("/v1/bookings")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Bookings for Customer", description = "Booking for Customer operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerBookingsResourceV1 extends BaseResource {

    BookingService bookingService;

    @RolesAllowed(ROLE_UPDATE_BOOKING) // Only Customers
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update booking", description = "API to update an exist booking by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> update(@PathParam("id") String id, BookingRequest.UpdatedInfo body) {
        // Check owner of booking
        hasRole(IdentityRole.CUSTOMER);
        return bookingService.update(id, body).onItem().transform(updatedBookingId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Update", "booking"))
                .data(updatedBookingId).build()
            ).build());

    }

    @RolesAllowed(ROLE_UPDATE_BOOKING) // Only Customers
    @DELETE
    @Path("/{id}/soft")
    @Operation(summary = "Delete booking", description = "API to soft delete a booking by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> softDelete(@PathParam("id") String id) {
        // Check owner of booking
        hasRole(IdentityRole.CUSTOMER);
        return bookingService.softDelete(id).onItem().transform(deletedBookingId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Soft delete", "booking"))
                .data(deletedBookingId).build()
            ).build());

    }

    @RolesAllowed(ROLE_DELETE_BOOKING) // Only Customers
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete booking", description = "API to soft delete a booking by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> delete(@PathParam("id") String id) {
        // Check owner of booking
        hasRole(IdentityRole.CUSTOMER);
        return bookingService.delete(id).onItem().transform(deletedBookingId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Delete", "booking"))
                .data(deletedBookingId).build()
            ).build());
    }

    @RolesAllowed(ROLE_READ_BOOKING) // Only Customers
    @GET
    @Path("/{id}")
    @NoCache
    @Operation(summary = "Get my booking details", description = "API to get details of a booking of mine by id.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = BookingResponse.Details.class)))
    public Uni<RestResponse<CommonResponse<BookingResponse.Details>>> myBooking(@PathParam("id") String id) {
        // Check owner of booking
        hasRole(IdentityRole.CUSTOMER);
        return bookingService.details(id).onItem().transform(booking ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<BookingResponse.Details>builder().data(booking).build()).build()
        );
    }

    @RolesAllowed(ROLE_READ_BOOKING) // Only Customers
    @GET
    @Path("")
    @NoCache
    @Operation(summary = "Search my bookings", description = "API to search my bookings.")
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public Uni<RestResponse<PageResponse<BookingResponse.Details>>> searchMyBookings(
        @QueryParam("index") int index,
        @QueryParam("limit") int limit
    ) {
        hasRole(IdentityRole.CUSTOMER);
        return bookingService.search(index, limit).map(
            page -> RestResponse.ResponseBuilder.ok(page).build()
        );
    }

}
