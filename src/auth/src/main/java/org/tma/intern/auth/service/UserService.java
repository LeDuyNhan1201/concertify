package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.auth.dto.request.UserRequest;
import org.tma.intern.auth.dto.response.UserResponse;
import org.tma.intern.common.type.Region;

import java.util.List;

public interface UserService {

    Uni<UserResponse.Details> getUserByEmail(String email);

    Uni<String> signUp(UserRequest.Registration request);

    Uni<String> createUser(UserRequest.Creation request);

    Uni<String> delete(String id);

    Uni<List<String>> seedUsers(int count, IdentityGroup group, Region region);

}
