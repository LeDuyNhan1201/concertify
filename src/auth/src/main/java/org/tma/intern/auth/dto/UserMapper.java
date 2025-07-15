package org.tma.intern.auth.dto;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.common.base.IdentityUser;
import org.tma.intern.common.base.BaseMapper;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper {

    UserResponse.Detail toDto(IdentityUser entity);

    IdentityUser toEntity(UserRequest.Creation dto);

}