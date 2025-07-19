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
import org.jboss.resteasy.reactive.RestResponse;
import org.tma.intern.common.base.BaseResource;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.service.SeatService;

import java.util.List;

@Path("/v1/seats")
@SecuritySchemes(value = {
    @SecurityScheme(securitySchemeName = "bearerToken",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer"
    )}
)
@Tag(name = "Seats", description = "Seat operations")
@Produces(MediaType.APPLICATION_JSON)
@APIResponse(responseCode = "401", description = "Unauthenticated", content = @Content())
@APIResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = String.class)))
@APIResponse(responseCode = "501", description = "Failed", content = @Content(schema = @Schema(implementation = String.class)))
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatsResourceV1 extends BaseResource {

    SeatService seatService;

    static final String SEAT_UPDATE_ROLE = "concert:seat:update";

    @RolesAllowed(SEAT_UPDATE_ROLE) // Only Customers
    @PUT
    @Path("/hold/{concertId}/concert")
    @Operation(summary = "Hold seats", description = "API to hold seats by list seat id.")
    @APIResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = String.class)))
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<CommonResponse<List<String>>>> holdSeats(@PathParam("concertId") String concertId, ConcertRequest.SeatIds body) {
        // Check region
        return seatService.hold(body, concertId).onItem().transform(resultIds ->
            RestResponse.ResponseBuilder.ok(CommonResponse.<List<String>>builder()
                .message(locale.getMessage("Action.Success", "Hold", "seats"))
                .data(resultIds).build()
            ).build());
    }

}
