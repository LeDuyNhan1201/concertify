package org.tma.intern.concert.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.common.base.BaseMapper;
import org.tma.intern.concert.data.Concert;
import org.tma.intern.concert.data.Seat;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConcertMapper extends BaseMapper {

    ConcertResponse.PreviewSeat toPreviewDto(Seat entity);

    @Mapping(source = "createdBy", target = "ownerId")
    ConcertResponse.Preview toPreviewDto(Concert entity);

    @Mapping(source = "createdBy", target = "ownerId")
    ConcertResponse.DetailsWithSeats toDetailsWithSeatsDto(Concert entity);

    ConcertResponse.PreviewWithSeats toPreviewWithSeatsDto(Concert entity);

    Concert toEntity(ConcertRequest.Info dto);

}