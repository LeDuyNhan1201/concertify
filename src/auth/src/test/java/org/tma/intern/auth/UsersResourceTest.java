package org.tma.intern.auth;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.tma.intern.auth.api.UsersResource;
import org.tma.intern.auth.data.Region;
import org.tma.intern.common.helper.StringHelper;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestHTTPEndpoint(UsersResource.class)
@QuarkusTest
class UsersResourceTest {

    @Test
    void testCreateAndFetchUser() {
        // Tạo user mới với email và password
        String email = "test@gmail.com";
        String password = "123456";

        Response response = given()
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
            .statusCode(201)
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH,"Create", "user")))
            .extract().response();

        // Lấy user ID từ response
        String userId = response.jsonPath().getString("data");

        // Gọi API /{email} để lấy thông tin user
        given()
            .when().get("/{email}", email)
            .then().log().all()
            .statusCode(200)
            .body("id", is(userId))
            .body("email", is(email))
            .body("region", is(Region.EN.value));

        given()
            .when().delete("/{id}", userId)
            .then().log().all()
            .statusCode(200)
            .body("data", is(userId));
    }

}