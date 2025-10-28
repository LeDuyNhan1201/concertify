package org.tma.intern.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.tma.intern.auth.dto.request.UserRequest;
import org.tma.intern.auth.dto.response.UserResponse;
import org.tma.intern.common.base.BaseMapper;
import org.tma.intern.auth.data.IdentityUser;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper {

    UserResponse.Details toDetail(IdentityUser entity);

    IdentityUser toIdentityUser(UserRequest.Creation dto);

    IdentityUser toIdentityUser(UserRequest.Registration dto);

}