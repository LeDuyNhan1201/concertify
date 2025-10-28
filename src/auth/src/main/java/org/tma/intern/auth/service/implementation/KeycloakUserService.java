package org.tma.intern.auth.service.implementation;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.tma.intern.auth.data.IdentityAdminClient;
import org.tma.intern.auth.data.IdentityUser;
import org.tma.intern.auth.dto.request.UserRequest;
import org.tma.intern.auth.dto.response.UserResponse;
import org.tma.intern.auth.mapper.UserMapper;
import org.tma.intern.auth.service.UserService;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KeycloakUserService extends BaseService implements UserService {

    IdentityAdminClient keycloakAdminClient;

    UserMapper userMapper;

    Faker faker;

    @Override
    public Uni<UserResponse.Details> getUserByEmail(String email) {
        return super.assertNotFound(
            keycloakAdminClient.getUserByEmail(email),
            IdentityUser.class
        ).map(userMapper::toDetail);
    }

    @Override
    public Uni<String> signUp(UserRequest.Registration request) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(
                keycloakAdminClient.createUserIntoGroup(
                    userMapper.toIdentityUser(request),
                    IdentityGroup.CUSTOMERS,
                    Region.valueOf(locale.getCountry())
                )
            )
            .action(Action.CREATE)
            .entityType(IdentityUser.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<String> createUser(UserRequest.Creation request) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(
                keycloakAdminClient.createUserIntoGroup(
                    userMapper.toIdentityUser(request),
                    request.groupType(),
                    request.region()
                )
            )
            .action(Action.CREATE)
            .entityType(IdentityUser.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<String> delete(String id) {
        var uniActionCombine = UniActionCombine.<String>builder()
            .uni(keycloakAdminClient.deleteUserById(id))
            .action(Action.DELETE)
            .entityType(IdentityGroup.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

    @Override
    public Uni<List<String>> seedUsers(int count, IdentityGroup group, Region region) {
        List<IdentityUser> fakeUsers = IntStream.range(0, count).mapToObj(index -> {
                String lastName = faker.name().lastName().trim();
                return IdentityUser.builder()
                    .email(
                        String.format(
                            "%s_%s.%s.%s@gmail.com",
                            lastName.replace(" ", "").toLowerCase(),
                            index,
                            group.type,
                            region.country
                        )
                    )
                    .password("123")
                    .firstName(group.name())
                    .lastName(lastName)
                    .build();
            })
            .toList();

        var uniActionCombine = UniActionCombine.<List<String>>builder()
            .uni(
                keycloakAdminClient.createUsers(
                    fakeUsers,
                    group,
                    region
                ).collect().asList()
            )
            .action(Action.CREATE)
            .entityType(IdentityGroup.class)
            .build();
        return super.assertActionFail(uniActionCombine);
    }

}
