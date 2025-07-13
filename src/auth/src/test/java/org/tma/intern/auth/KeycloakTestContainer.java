package org.tma.intern.auth;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.BindMode;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@TestInstance(Lifecycle.PER_CLASS)
public class KeycloakTestContainer implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> keycloak;

    @Override
    public Map<String, String> start() {

        try (GenericContainer<?> keycloak = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:latest"))
            .withExposedPorts(8080, 9990)
            .withFileSystemBind("src/test/resources/test-realm.json", "/opt/keycloak/data/import/realm-import.json", BindMode.READ_ONLY)
            .withEnv("KC_HTTP_MANAGEMENT_PORT", "9990")
            .withEnv("KC_METRICS_ENABLED", "true")
            .withEnv("KC_HEALTH_ENABLED", "true")
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "ldnhan")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "123")
            .withEnv("KC_PROXY", "edge")
            .withEnv("KC_CORS", "true")
            .withEnv("KC_CORS_ORIGINS", "*")
            .withEnv("KEYCLOAK_IMPORT", "/opt/keycloak/data/import/realm-import.json")
            .withCommand("start-dev", "--health-enabled=true", "--import-realm")
//            .waitingFor(
//                Wait.forHttp("/health/ready")
//                    .forPort(9990)
//                    .forStatusCode(200)
//                    .withStartupTimeout(Duration.ofSeconds(90))
//            )
            .waitingFor(Wait.forListeningPort())
            /*.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(40000), new ExposedPort(8080)),
                    new PortBinding(Ports.Binding.bindPort(49990), new ExposedPort(9990))
                )
            ))*/) {

            keycloak.start();

            String baseUrl = String.format("http://%s:%s", keycloak.getHost(), keycloak.getMappedPort(8080));
            return Map.ofEntries(
                // ---------- Admin Client ----------
                Map.entry("quarkus.keycloak.admin-client.server-url", baseUrl + "/"),
                Map.entry("quarkus.keycloak.admin-client.realm", "concertify"),
                Map.entry("quarkus.keycloak.admin-client.grant-type", "CLIENT_CREDENTIALS"),
                Map.entry("quarkus.keycloak.admin-client.client-id", "auth-service"),
                Map.entry("quarkus.keycloak.admin-client.client-secret", "X4Y6nlgqf7dX6ajQ449gRRu9fF0gfBw2"),

                // ---------- OIDC ----------
                Map.entry("quarkus.oidc.auth-server-url", baseUrl + "/realms/concertify"),
                Map.entry("quarkus.oidc.client-id", "auth-service"),
                Map.entry("quarkus.oidc.credentials.secret", "X4Y6nlgqf7dX6ajQ449gRRu9fF0gfBw2"),
                Map.entry("quarkus.oidc.authentication.user-info-required", "false"),
                Map.entry("quarkus.oidc.roles.source", "accesstoken"),
                Map.entry("quarkus.oidc.roles.role-claim-path", "roles"),

                // ---------- Keycloak Policy Enforcer ----------
                Map.entry("quarkus.keycloak.policy-enforcer.lazy-load-paths", "false"),

                // ---------- Public Endpoint Rule ----------
                Map.entry("quarkus.keycloak.policy-enforcer.paths.1.enforcement-mode", "DISABLED"),
                Map.entry("quarkus.keycloak.policy-enforcer.paths.1.paths", "/auth/hello")
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to start Keycloak container", e);
        }
    }

    @Override
    public void stop() {
        if (keycloak != null) {
            keycloak.stop();
        }
    }
}
