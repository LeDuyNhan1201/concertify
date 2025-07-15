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
import org.tma.intern.booking.dto.BookingRequest;
import org.tma.intern.booking.dto.BookingResponse;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.BookingStatus;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.dto.PageResponse;

import java.util.List;

@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Path("/v1")
@Tag(name = "Bookings", description = "Booking operations")
@APIResponse(responseCode = "500", description = "Failed",
    content = @Content(schema = @Schema(implementation = String.class)))
@Produces(MediaType.APPLICATION_JSON)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class BookingsResourceV1 extends BaseResource {

    BookingService bookingService;

    @RolesAllowed("booking:create")
    @POST
    @Path("")
    @Operation(summary = "Create booking", description = "API to create a new booking.")
    @APIResponse(responseCode = "201", description = "Success",
        content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> create(BookingRequest.Body body) {
        checkRegion();
        return bookingService.create(body).onItem().transform(id ->
            RestResponse.ResponseBuilder.create(RestResponse.Status.CREATED, CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Create", "booking"))
                .data(id).build()
            ).build());
    }

    @RolesAllowed("booking:edit")
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update booking", description = "API to update an exist booking by id.")
    @APIResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = String.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> update(@PathParam("id") String id, BookingRequest.Update body) {
        checkRegion();
        return bookingService.update(id, body).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Update", "booking"))
                .data(id).build()
            ).build());

    }

    @RolesAllowed("booking:edit")
    @PATCH
    @Path("/{id}/{status}")
    @Operation(summary = "Update booking", description = "API to update an exist booking by id.")
    @APIResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = String.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<String>>> update(@PathParam("id") String id, @PathParam("status") BookingStatus status) {
        checkRegion();
        return bookingService.update(id, status).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Update", "booking"))
                .data(id).build()
            ).build());

    }

    @RolesAllowed("booking:edit")
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete booking", description = "API to soft delete a booking by id.")
    @APIResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = String.class)))
    public Uni<RestResponse<CommonResponse<String>>> softDelete(@PathParam("id") String id) {
        return bookingService.softDelete(id).onItem().transform(resultId ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<String>builder()
                .message(locale.getMessage("Action.Success", "Soft delete", "booking"))
                .data(id).build()
            ).build());

    }

    @RolesAllowed("booking:view")
    @GET
    @Path("/{id}")
    @NoCache
    @Operation(summary = "Get booking details", description = "API to get details of a booking by id.")
    @APIResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = BookingResponse.Detail.class)))
    public Uni<RestResponse<CommonResponse<BookingResponse.Detail>>> details(@PathParam("id") String id) {
        return bookingService.findById(id).onItem().transform(booking ->
            RestResponse.ResponseBuilder.ok(
                CommonResponse.<BookingResponse.Detail>builder().data(booking).build()
            ).build());
    }

    @RolesAllowed("booking:view")
    @GET
    @Path("")
    @NoCache
    @Operation(summary = "Get bookings page", description = "API to get a page of bookings by index & limit.")
    @APIResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public Uni<RestResponse<PageResponse<BookingResponse.Detail>>> paging(
        @QueryParam("index") int index,
        @QueryParam("limit") int limit) {
        return bookingService.findAll(index, limit).onItem().transform(page ->
            RestResponse.ResponseBuilder.ok(page).build());
    }

    @GET
    @Path("/seed/{count}")
    @NoCache
    @Operation(summary = "Seed bookings", description = "API to seed bookings with count.")
    @APIResponse(responseCode = "200", description = "Success",
        content = @Content(schema = @Schema(implementation = Integer.class)))
    public Uni<RestResponse<CommonResponse<List<String>>>> seed(int count) {
        return bookingService.seedData(count).onItem().transform(ids ->
            RestResponse.ok(CommonResponse.<List<String>>builder().data(ids).build()));
    }

}
