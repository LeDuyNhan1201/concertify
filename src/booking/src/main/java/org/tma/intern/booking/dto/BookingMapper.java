package org.tma.intern.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.booking.entity.Booking;
import org.tma.intern.booking.entity.BookingItem;
import org.tma.intern.common.base.BaseMapper;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper extends BaseMapper {

    BookingResponse.Detail toDto(Booking entity);

    BookingItemResponse.Detail toDto(BookingItem entity);

    BookingItem toEntity(BookingItemRequest.Body dto);

}