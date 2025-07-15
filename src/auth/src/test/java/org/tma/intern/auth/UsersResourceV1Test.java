package org.tma.intern.auth;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.response.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.tma.intern.auth.api.UsersResourceV1;
import org.tma.intern.common.dto.IdentityGroup;
import org.tma.intern.common.dto.Region;
import org.tma.intern.common.helper.StringHelper;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestHTTPEndpoint(UsersResourceV1.class)
@QuarkusTest
class UsersResourceV1Test {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    public String getAccessToken(String user) {
        return keycloakClient.getAccessToken(user, "123", "web-app");
    }

    static final String ADMIN_EMAIL = "admin@gmail.com";

    @Test
    void createGroupAndUser_isOrganizerInUS_Success() {

        given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .header("Content-Type", "application/json")
            .body("""
                {
                    "region": "%s",
                    "group": "%s"
                }
                """.formatted(Region.US.name(), IdentityGroup.ORGANIZERS.name()))
            .when().post("/groups")
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Create", "group")))
            .extract().response();

        // Tạo user mới với email và password
        String email = "test@gmail.com";
        String password = "123456";

        Response response = given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body("""
                {
                    "email": "%s",
                    "password": "%s",
                    "firstName": "Test",
                    "lastName": "User",
                    "group": "%s"
                }
                """.formatted(email, password, IdentityGroup.ORGANIZERS.name()))
            .when().post()
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Create", "user")))
            .extract().response();

        // Lấy user ID từ response
        String userId = response.jsonPath().getString("data");

        // Gọi API /{email} để lấy thông tin user
        given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .when().get("/{email}", email)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .body("data.id", is(userId))
            .body("data.email", is(email));

        given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .when().delete("/{id}", userId)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Delete", "user")))
            .body("data", is(userId));
    }

}