package org.tma.intern.auth;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.tma.intern.auth.api.UsersResource;
import org.tma.intern.auth.data.Region;
import org.tma.intern.common.helper.OidcTestClient;
import org.tma.intern.common.helper.StringHelper;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestHTTPEndpoint(UsersResource.class)
@QuarkusTest
class UsersResourceTest {

    OidcTestClient oidcTestClient = new OidcTestClient();

    static final String ADMIN_EMAIL = "admin@gmail.com";

    @Test
    void testCreateAndFetchUser() {
        // Tạo user mới với email và password
        String email = "test@gmail.com";
        String password = "123456";

        Response response = given()
            .auth().oauth2(oidcTestClient.getAccessToken(ADMIN_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en")
            .body("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password))
            .when().post()
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH,"Create", "user")))
            .extract().response();

        // Lấy user ID từ response
        String userId = response.jsonPath().getString("data");

        // Gọi API /{email} để lấy thông tin user
        given()
            .auth().oauth2(oidcTestClient.getAccessToken(ADMIN_EMAIL))
            .when().get("/{email}", email)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .body("data.id", is(userId))
            .body("data.email", is(email))
            .body("data.region", is(Region.EN.name()));

        given()
            .auth().oauth2(oidcTestClient.getAccessToken(ADMIN_EMAIL))
            .when().delete("/{id}", userId)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH,"Delete", "user")))
            .body("data", is(userId));
    }

}