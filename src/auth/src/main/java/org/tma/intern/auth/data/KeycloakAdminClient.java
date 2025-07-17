package org.tma.intern.auth.data;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.*;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KeycloakAdminClient implements IdentityAdminClient {

    Keycloak keycloak;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    @NonFinal
    String REALM;

    static final String GROUP_PREFIX = "/global";

    @Override
    public Multi<String> getRoles() {
        return Multi.createFrom().items(() ->
            keycloak.realm(REALM).roles().list()
                .stream()
                .map(RoleRepresentation::getName)
        ).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Override
    public Uni<IdentityUser> getUserByEmail(String email) {
        return Uni.createFrom().item(() ->
            getUserByEmailBlocking(email)).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

//    @Override
//    public Uni<List<IdentityUser>> getUsersByGroup(IdentityGroup group) {
//        return Uni.createFrom().item(() ->
//            getUsersByGroupBlocking(group)).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
//    }
//
//    private List<IdentityUser> getUsersByGroupBlocking(IdentityGroup group) {
//        GroupsResource users = keycloak.realm(REALM).groups();
//        try {
//            UserRepresentation response = users.group().members();
//            return IdentityUser.builder()
//                .id(response.getId())
//                .email(response.getEmail())
//                .region(Region.valueOf(response.getAttributes().get("region").get(0).toUpperCase()))
//                .build();
//        } catch (Exception e) {
//            log.error("Failed to fetch users of {} group in Keycloak", group, e);
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public Uni<String> createGroup(IdentityGroup group, Region region) {
        return Uni.createFrom().item(() -> createGroupBlocking(group, region))
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Override
    public Uni<String> createUser(IdentityUser user, IdentityGroup group, Region region) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setCreatedTimestamp(Instant.now().toEpochMilli());
        newUser.setEnabled(true);
        newUser.setEmailVerified(true);
        newUser.setCredentials(Collections.singletonList(createPasswordCredential(user.getPassword())));
        newUser.setGroups(Stream.of(group).map(identityGroup -> formatFullPathGroupName(group, region)).toList());

        return Uni.createFrom().item(() -> {
                if (findGroupByPath(formatFullPathGroupName(group, region)).isEmpty())
                    throw new RuntimeException("Group does not exist: " + group.type + " in region: " + region.country);
                return createUserBlocking(newUser);
            })
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Override
    public Uni<String> deleteUser(String id) {
        return Uni.createFrom().item(() ->
            deleteUserBlocking(id)).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Override
    public Multi<String> createUsers(List<IdentityUser> entities, IdentityGroup group, Region region) {
        List<Uni<String>> creations = entities.stream()
            .map(entity -> createUser(entity, group, region)).toList();

        return Uni.combine().all().unis(creations)
            .with(objects -> objects.stream().map(Object::toString).toList())
            .onItem().transformToMulti(Multi.createFrom()::iterable);
    }

    private IdentityUser getUserByEmailBlocking(String email) {
        UsersResource users = keycloak.realm(REALM).users();
        try {
            UserRepresentation response = users.searchByEmail(email, true).get(0);
            return IdentityUser.builder()
                .id(response.getId())
                .email(response.getEmail())
                .build();
        } catch (Exception e) {
            log.error("Failed to fetch user in Keycloak: {}", email, e);
            throw new RuntimeException(e);
        }
    }

    private String createUserBlocking(UserRepresentation user) {
        UsersResource users = keycloak.realm(REALM).users();
        try (Response response = users.create(user)) {
            return handleCreationResponse(response, "User");
        }
    }

    private String deleteUserBlocking(String id) {
        UsersResource users = keycloak.realm(REALM).users();
        try (Response response = users.delete(id)) {
            int status = response.getStatus();
            if (status != Response.Status.NO_CONTENT.getStatusCode())
                throw new RuntimeException("Keycloak user deletion failed with status: " + status);
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user in Keycloak: " + id, e);
        }
    }

    private String createGroupBlocking(IdentityGroup group, Region region) {
        GroupsResource groups = keycloak.realm(REALM).groups();
        Optional<GroupRepresentation> checkRegionGroup =
            findGroupByPath(MessageFormat.format("{0}/{1}", GROUP_PREFIX, region.country));
        if (checkRegionGroup.isPresent())
            return createSpecificGroup(groups, checkRegionGroup.get().getId(), group, region);
        else {
            // Không có /global/{region}
            Optional<GroupRepresentation> globalGroup = findGroupByPath(GROUP_PREFIX);
            if (globalGroup.isEmpty()) {
                throw new RuntimeException("Root group /global does not exist !!!");
            }
            String regionGroupId = createSubGroup(groups, globalGroup.get().getId(), region.country);
            return createSpecificGroup(groups, regionGroupId, group, region);
        }
    }

    private String createSpecificGroup(GroupsResource groups, String parentId, IdentityGroup group, Region region) {
        RolesResource roles = keycloak.realm(REALM).roles();
        IdentityRole role = generateRoleBaseOnGroup(group);

        String roleName = formatRealmRoleName(role, region);
        RoleRepresentation realmRole = getOrCreateRealmRole(roles, roleName, role);

        String groupName = formatFullPathGroupName(group, region);
        if (findGroupByPath(groupName).isPresent()) throw new RuntimeException("Group is already existed !!!");

        // Create & assign realm role to group
        String groupId = createSubGroup(groups, parentId, group.type);
        groups.group(groupId).roles().realmLevel().add(List.of(realmRole));

        return groupId;
    }

    private String createSubGroup(GroupsResource groups, String parentId, String groupName) {
        GroupRepresentation newGroup = new GroupRepresentation();
        newGroup.setName(groupName);

        try (Response response = groups.group(parentId).subGroup(newGroup)) {
            return handleCreationResponse(response, "Region group");
        }
    }

    private String formatRealmRoleName(IdentityRole role, Region region) {
        return MessageFormat.format("{0}_{1}", role.prefix, region.country);
    }

    private String formatFullPathGroupName(IdentityGroup group, Region region) {
        return MessageFormat.format("{0}/{1}/{2}", GROUP_PREFIX, region.country, group.type);
    }

    private CredentialRepresentation createPasswordCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }

    private RoleRepresentation getOrCreateRealmRole(RolesResource roles, String roleName, IdentityRole roleType) {
        try {
            return roles.get(roleName).toRepresentation();
        } catch (Exception e) {
            log.warn("Failed to check role existence: {} caused by {}", roleName, e.getMessage());

            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setClientRole(false);
            newRole.setName(roleName);
            roles.create(newRole);

            RoleRepresentation createdRole = roles.get(roleName).toRepresentation();

            try {
                assignClientRolesToRealmRole(IdentityClient.CONCERT, roleType, createdRole.getName());
                assignClientRolesToRealmRole(IdentityClient.BOOKING, roleType, createdRole.getName());
            } catch (Exception exception) {
                log.error("Cannot assign client roles caused by {}", exception.getMessage());
                roles.deleteRole(createdRole.getName());
                throw new RuntimeException("Cannot assign client roles", exception);
            }

            return createdRole;
        }
    }

    private String handleCreationResponse(Response response, String entityName) {
        int status = response.getStatus();
        if (status == Response.Status.CREATED.getStatusCode()) {
            String location = response.getHeaderString("Location");
            if (location == null)
                throw new RuntimeException(entityName + " creation succeeded but no Location header returned");
            return location.substring(location.lastIndexOf('/') + 1);

        } else throw new RuntimeException("Keycloak " + entityName + " creation failed with status: "
            + status + " - " + response.readEntity(String.class));
    }

    private IdentityRole generateRoleBaseOnGroup(IdentityGroup group) {
        return switch (group) {
            case CUSTOMERS -> IdentityRole.CUSTOMER;
            case ORGANIZERS -> IdentityRole.ORGANIZER;
            case ADMINISTRATORS -> IdentityRole.ADMINISTRATOR;
        };
    }

    private List<ClientScope> generateClientScopeBaseOnRole(IdentityClient identityClient, IdentityRole role) {
        return switch (role) {
            case CUSTOMER -> switch (identityClient) {
                case AUTH -> List.of(ClientScope.READ, ClientScope.UPDATE);
                case CONCERT -> List.of(ClientScope.VIEW);
                case BOOKING ->
                    List.of(ClientScope.VIEW, ClientScope.READ, ClientScope.CREATE, ClientScope.UPDATE, ClientScope.DELETE);
            };
            case ORGANIZER -> switch (identityClient) {
                case AUTH -> List.of(ClientScope.READ, ClientScope.UPDATE);
                case CONCERT ->
                    List.of(ClientScope.VIEW, ClientScope.READ, ClientScope.CREATE, ClientScope.UPDATE, ClientScope.DELETE);
                case BOOKING -> List.of(ClientScope.VIEW, ClientScope.READ, ClientScope.UPDATE);
            };
            case ADMINISTRATOR -> switch (identityClient) {
                case AUTH ->
                    List.of(ClientScope.VIEW, ClientScope.READ, ClientScope.CREATE, ClientScope.UPDATE, ClientScope.DELETE);
                case CONCERT, BOOKING -> List.of();
            };
        };
    }

    private IdentityClient generateClientIdBaseOnResourceType(ResourceType resourceType) {
        return switch (resourceType) {
            case CONCERT -> IdentityClient.CONCERT;
            case BOOKING -> IdentityClient.BOOKING;
        };
    }

    private String findGroupId(String groupPath) {
        return findGroupByPath(groupPath).map(GroupRepresentation::getId)
            .orElseThrow(() -> new IllegalStateException("Group unexpectedly not found"));
    }

    private Optional<GroupRepresentation> findDirectChildByName(String parentId, String childName) {
        List<GroupRepresentation> subGroups = keycloak.realm(REALM).groups().group(parentId)
            .getSubGroups(0, 200, true);

        return subGroups.stream().filter(group ->
            group.getName().equalsIgnoreCase(childName)).findFirst();
    }

    public Optional<GroupRepresentation> findGroupByPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) return Optional.empty();

        // Split path, skip empty parts
        String[] parts = path.split("/");
        List<String> names = Arrays.stream(parts).filter(s -> !s.isBlank()).toList();

        if (names.isEmpty()) return Optional.empty();

        // Step 1: Find top-level group matching first part
        List<GroupRepresentation> topGroups = keycloak.realm(REALM).groups().groups();

        Optional<GroupRepresentation> currentOpt = topGroups.stream().filter(group ->
            group.getName().equalsIgnoreCase(names.get(0))).findFirst();

        if (currentOpt.isEmpty()) return Optional.empty();

        GroupRepresentation current = currentOpt.get();

        // Step 2: Walk down the path
        for (int i = 1; i < names.size(); i++) {
            String nextName = names.get(i);
            Optional<GroupRepresentation> childOpt = findDirectChildByName(current.getId(), nextName);

            if (childOpt.isEmpty()) return Optional.empty();
            current = childOpt.get();
        }
        return Optional.of(current);
    }

    private RoleRepresentation getClientRole(String clientId, String roleName) {
        // 1. Find client
        ClientRepresentation client = keycloak.realm(REALM)
            .clients()
            .findByClientId(clientId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        // 2. Get role
        return keycloak.realm(REALM)
            .clients()
            .get(client.getId())
            .roles()
            .get(roleName)
            .toRepresentation();
    }

    private void assignClientRolesToRealmRole(IdentityClient client, IdentityRole role, String realmRoleName) {
        generateClientScopeBaseOnRole(client, role).forEach(clientScope -> {
            // Get the client role
            String clientRoleName = MessageFormat.format("{0}:{1}", client.rolePrefix, clientScope.value);
            RoleRepresentation clientRole = getClientRole(client.id, clientRoleName);

            // Add as composite to realm role
            keycloak.realm(REALM).roles().get(realmRoleName).addComposites(List.of(clientRole));

            log.info("Added client role '{}' from client '{}' to realm role '{}'",
                clientRoleName, client.id, realmRoleName);
        });
    }

    public String createConcertResourceBlocking(String concertId, String region) {
        String clientId = "concert-service";

        // 1. Find the client
        List<ClientRepresentation> clients = keycloak.realm(REALM)
            .clients()
            .findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Client not found: " + clientId);
        }
        String clientUuid = clients.get(0).getId();

        // 2. Get Authorization Resource
        AuthorizationResource authz = keycloak.realm(REALM)
            .clients()
            .get(clientUuid)
            .authorization();

        ResourcesResource resources = authz.resources();

        // 3. Build ResourceRepresentation
        ResourceRepresentation resource = new ResourceRepresentation();
        resource.setName("Concert " + concertId);
        resource.setType("concert");
        resource.setUris(Set.of("/concerts/" + concertId));
        resource.setScopes(Set.of(
            new ScopeRepresentation("view"),
            new ScopeRepresentation("edit"),
            new ScopeRepresentation("approve")));
        resource.setOwnerManagedAccess(false);
        resource.setAttributes(Map.of("region", List.of(region)));

        // 4. Create Resource
        try (Response response = resources.create(resource)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                String location = response.getHeaderString("Location");
                if (location == null) {
                    throw new RuntimeException("No Location header returned for created resource");
                }
                return location.substring(location.lastIndexOf('/') + 1);
            } else {
                String errorMessage = response.readEntity(String.class);
                throw new RuntimeException("Concert resource creation failed with status: "
                    + response.getStatus() + " - " + errorMessage);
            }
        }
    }

    private String createResourceBlocking(ResourceType resourceType, Region region) {
        ClientsResource clients = keycloak.realm(REALM).clients();
        IdentityClient identityClient = generateClientIdBaseOnResourceType(resourceType);

        ResourceRepresentation resource = new ResourceRepresentation();
        resource.setName(MessageFormat.format("{0} {1}", region.name(), resourceType.name));
        resource.setType(MessageFormat.format("urn:{0}:resources:{1}_{2}", identityClient, resourceType.type, region.country));
        resource.setOwnerManagedAccess(true);
        resource.setScopes(Set.of(
            new ScopeRepresentation("create"),
            new ScopeRepresentation("read"),
            new ScopeRepresentation("update"),
            new ScopeRepresentation("delete")));

        try (Response response = keycloak.realm(REALM)
            .clients()
            .get(clients.findByClientId(identityClient.id).get(0).getId())
            .authorization()
            .resources()
            .create(resource)) {
            int status = response.getStatus();

            if (status == Response.Status.CREATED.getStatusCode()) {
                String location = response.getHeaderString("Location");
                if (location == null) {
                    throw new RuntimeException("Resource creation succeeded but no Location header returned");
                }
                return location.substring(location.lastIndexOf('/') + 1);
            } else {
                String error = response.readEntity(String.class);
                throw new RuntimeException("Keycloak Resource creation failed with status: " + status + " - " + error);
            }
        }
    }

}
