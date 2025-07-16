package org.tma.intern.concert.dto;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.common.base.BaseMapper;
import org.tma.intern.concert.data.Concert;
import org.tma.intern.concert.data.Seat;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConcertMapper extends BaseMapper {

    ConcertResponse.PreviewSeat toPreviewDto(Seat entity);

    ConcertResponse.Preview toPreviewDto(Concert entity);

    ConcertResponse.Detail toDetailDto(Concert entity);

    Concert toEntity(ConcertRequest.Body dto);

}