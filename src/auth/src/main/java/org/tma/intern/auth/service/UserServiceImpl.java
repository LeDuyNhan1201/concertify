package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.tma.intern.auth.dto.UserMapper;
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.dto.UserResponse;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.auth.data.IdentityUser;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.common.type.identity.IdentityGroup;
import org.tma.intern.common.type.Region;

import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl extends BaseService implements UserService {

    IdentityAdminClient keycloakAdminClient;

    UserMapper userMapper;

    Faker faker;

    @Override
    public Uni<UserResponse.Detail> findByEmail(String email) {
        return keycloakAdminClient.getUserByEmail(email)
            .onItem().transform(userMapper::toDto)
            .onFailure().transform(throwable -> {
                log.error("Failed to find user by email: {}", email, throwable);
                throw new HttpException(AppError.RESOURCE_NOT_FOUND,
                    Response.Status.NOT_FOUND, throwable, "user");
            });
    }

    @Override
    public Uni<String> create(UserRequest.Creation request) {
        return keycloakAdminClient.createUser(userMapper.toEntity(request), request.group(), request.region())
            .onFailure().transform(throwable -> {
                log.error("Failed to create user: {} caused by {}", request.email(), throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Create", "user");
            });
    }

    @Override
    public Uni<String> createGroup(UserRequest.GroupCreation request) {
        return keycloakAdminClient.createGroup(request.group(), request.region())
            .onFailure().transform(throwable -> {
                log.error("Failed to create group: {} caused by {}",
                    request.region().country + "/" + request.group().type, throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Create", "group");
            });
    }

    @Override
    public Uni<String> delete(String id) {
        return keycloakAdminClient.deleteUser(id)
            .onFailure().transform(throwable -> {
                log.error("Failed to delete user: {} caused by {}", id, throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Delete", "user");
            });
    }

    @Override
    public Uni<List<String>> seedUsers(int count, IdentityGroup group, Region region) {
        List<IdentityUser> fakeUsers = IntStream.range(0, count)
            .mapToObj(i -> IdentityUser.builder()
                .email(faker.naruto().character().trim().replace(" ", "").toLowerCase() + "@gmail.com")
                .password("123456")
                .build())
            .toList();

        return keycloakAdminClient.createUsers(fakeUsers, group, region).onItem().invoke(
            id -> log.info("Created userId: {}", id)).collect().asList();
    }

}
