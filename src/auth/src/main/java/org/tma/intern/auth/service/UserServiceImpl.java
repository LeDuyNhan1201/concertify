package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.tma.intern.auth.data.IdentityGroup;
import org.tma.intern.auth.data.Region;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.auth.data.IdentityUser;
import org.tma.intern.auth.dto.UserMapper;
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.dto.UserResponse;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;

import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl extends BaseService implements UserService {

    IdentityAdminClient keycloakAdminClient;

    UserMapper userMapper;

    Faker faker = new Faker();

    @Override
    public Uni<UserResponse.Details> findByEmail(String email) {
        return keycloakAdminClient.getUserByEmail(email)
            .onItem().transform(userMapper::toDto)
            .onFailure().transform(throwable ->
                new HttpException(AppError.RESOURCE_NOT_FOUND,
                    Response.Status.NOT_FOUND, throwable, "user"));
    }

    @Override
    public Uni<String> create(UserRequest.Creation request) {
        return keycloakAdminClient.createUser(IdentityUser.builder()
                .email(request.email())
                .password(request.password())
                .region(Region.valueOf(locale.getRegion().toUpperCase())).build(), IdentityGroup.CUSTOMERS)
            .onFailure().transform(throwable ->
                new HttpException(AppError.ACTION_FAILED,
                    Response.Status.NOT_IMPLEMENTED, throwable, "Create", "user"));
    }

    @Override
    public Uni<String> delete(String id) {
        try {
            return keycloakAdminClient.deleteUser(id);
        } catch (Exception exception) {
            log.error("Keycloak user deletion failed:  {}", exception.getMessage());
            throw new HttpException(AppError.ACTION_FAILED,
                Response.Status.NOT_IMPLEMENTED, exception.getCause(), "Delete", "user");
        }
    }

    @Override
    public Uni<List<String>> seedUsers(int count, IdentityGroup... groups) {
        List<IdentityUser> fakeUsers = IntStream.range(0, count)
            .mapToObj(i -> IdentityUser.builder()
                .email(faker.naruto().character().trim().replace(" ", "").toLowerCase() + "@gmail.com")
                .password("123456")
                .build())
            .toList();

        return keycloakAdminClient.createUsers(fakeUsers, groups).onItem().invoke(
            id -> log.info("Created userId: {}", id)).collect().asList();
    }

}
