package org.tma.intern.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.booking.dto.request.BookingItemRequest;
import org.tma.intern.booking.dto.response.BookingItemResponse;
import org.tma.intern.booking.model.BookingItem;
import org.tma.intern.common.base.BaseMapper;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingItemMapper extends BaseMapper {

    BookingItemResponse.Details toDetailsDto(BookingItem entity);

    BookingItem toEntity(BookingItemRequest.Info dto);

}