package org.tma.intern.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.common.mapper.TypeRef;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.*;
import org.tma.intern.auth.dto.UserRequest;
import org.tma.intern.auth.dto.UserResponse;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.identity.IdentityGroup;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
class UsersAndGroupsResourceV1Test {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    public String getAccessToken(String user) {
        return keycloakClient.getAccessToken(user, "123", "web-app");
    }

    static final String ADMIN_EMAIL = "admin@gmail.com";

    static final String ORGANIZER_VN_EMAIL = "organizer.vn@gmail.com";

    static final String ORGANIZER1_VN_EMAIL = "organizer1.vn@gmail.com";

    static final String CUSTOMER_VN_EMAIL = "customer.vn@gmail.com";

    static final String CUSTOMER1_VN_EMAIL = "customer1.vn@gmail.com";

    static final String ORGANIZER_US_EMAIL = "organizer.us@gmail.com";

    static final String CUSTOMER_US_EMAIL = "customer.us@gmail.com";

    static final String USERS_PATH = "/v1/users";

    static final String GROUPS_PATH = "/v1/groups";

    String userId;

    String email = "customer.fr@gmail.com";

    @Order(1)
    @Test
    void createFranceCustomersGroup_withAdminInUS_Success() {
        given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body(new UserRequest.GroupCreation(IdentityGroup.CUSTOMERS, Region.FR))
            .when().post(GROUPS_PATH)
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Create", "group")))
            .extract().response();
    }

    @Order(2)
    @Test
    void createFranceCustomer_withAdminInUS_Success() {
        CommonResponse<String> response = given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body(new UserRequest.Creation(
                email,
                "123",
                "Customer",
                "FR",
                IdentityGroup.CUSTOMERS, Region.FR))
            .when().post(USERS_PATH)
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Create", "user")))
            .extract()
            .as(new TypeRef<>() {});

        // Lấy user ID từ response
        userId = response.getData();
    }

    @Order(3)
    @Test
    void getUserByEmail_withAdminInUS_Success() {
        CommonResponse<UserResponse.Detail> response = given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .when().get(USERS_PATH + "/{email}", email)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .extract()
            .as(new TypeRef<>() {});

        Assertions.assertEquals(userId, response.getData().getId());
        Assertions.assertEquals(email, response.getData().getEmail());
    }

    @Order(4)
    @Test
    void deleteUserById_withAdminInUS_Success() {
        given()
            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
            .when().delete(USERS_PATH + "/{id}", userId)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Delete", "user")))
            .body("data", is(userId));
    }

}