package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.auth.data.IdentityGroup;
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.dto.UserResponse;

import java.util.List;

public interface UserService {

    Uni<UserResponse.Details> findByEmail(String email);

    Uni<String> create(UserRequest.Creation request);

    Uni<String> delete(String id);

    Uni<List<String>> seedUsers(int count, IdentityGroup... groups);

}
