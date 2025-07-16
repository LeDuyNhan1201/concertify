package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.dto.UserResponse;
import org.tma.intern.common.type.Region;

import java.util.List;

public interface UserService {

    Uni<UserResponse.Detail> findByEmail(String email);

    Uni<String> create(UserRequest.Creation request);

    Uni<String> createGroup(UserRequest.GroupCreation request);

    Uni<String> delete(String id);

    Uni<List<String>> seedUsers(int count, IdentityGroup group, Region region);

}
