package org.tma.intern.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.booking.dto.request.BookingRequest;
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.booking.entity.Booking;
import org.tma.intern.common.base.BaseMapper;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper extends BaseMapper {

    @Mapping(source = "createdBy", target = "ownerId")
    BookingResponse.Detail toDto(Booking entity);

    Booking toEntity(BookingRequest.Body dto);

}