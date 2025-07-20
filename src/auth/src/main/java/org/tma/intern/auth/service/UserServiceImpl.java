package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.auth.data.IdentityUser;
import org.tma.intern.auth.dto.UserMapper;
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.dto.UserResponse;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

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
            .onFailure().transform(throwable -> {
                log.error("Failed to find user by email: {}", email, throwable);
                throw new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, throwable, "user");
            }).map(userMapper::toDto);
    }

    @Override
    public Uni<String> signUp(UserRequest.Registration request) {
        return keycloakAdminClient.createUser(userMapper.toEntity(request), IdentityGroup.CUSTOMERS, Region.valueOf(locale.getCountry()))
            .onFailure().transform(throwable -> {
                log.error("Failed to sign up user: {} caused by {}", request.email(), throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Sign up", "user");
            });
    }

    @Override
    public Uni<String> create(UserRequest.Creation request) {
        return keycloakAdminClient.createUser(userMapper.toEntity(request), request.group(), request.region())
            .onFailure().transform(throwable -> {
                log.error("Failed to create user: {} caused by {}", request.email(), throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Create", "user");
            });
    }

    @Override
    public Uni<String> createGroup(UserRequest.GroupCreation request) {
        return keycloakAdminClient.createGroup(request.group(), request.region())
            .onFailure().transform(throwable -> {
                log.error("Failed to create group: {} caused by {}", request.region().country + "/" + request.group().type, throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Create", "group");
            });
    }

    @Override
    public Uni<String> assignClientRole(UserRequest.ClientRoleScope request) {
        return keycloakAdminClient.assignClientRole(request.client(), request.scope(), request.realmRoleName())
            .onFailure().transform(throwable -> {
                log.error("Failed to assign client role to realm role: {} caused by {}", request.realmRoleName(), throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Assign", "client role");
            });
    }

    @Override
    public Uni<String> delete(String id) {
        return keycloakAdminClient.deleteUser(id)
            .onFailure().transform(throwable -> {
                log.error("Failed to delete user: {} caused by {}", id, throwable.getMessage());
                throw new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Delete", "user");
            });
    }

    @Override
    public Uni<List<String>> seedUsers(int count, IdentityGroup group, Region region) {
        List<IdentityUser> fakeUsers = IntStream.range(0, count).mapToObj(index -> {
                String lastName = faker.name().lastName().trim();
                return IdentityUser.builder()
                    .email(String.format("%s_%s.%s.%s@gmail.com",
                        lastName.replace(" ", "").toLowerCase(), index, group.type, region.country))
                    .password("123")
                    .firstName(group.name())
                    .lastName(lastName)
                    .build();
            })
            .toList();

        return keycloakAdminClient.createUsers(fakeUsers, group, region).onItem().invoke(id ->
            log.info("Created userId: {}", id)
        ).collect().asList();
    }

}
