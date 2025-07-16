package org.tma.intern.concert;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.response.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.concert.api.ConcertsResourceV1;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestHTTPEndpoint(ConcertsResourceV1.class)
@QuarkusTest
class ConcertsResourceV1Test {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    public String getAccessToken(String user) {
        return keycloakClient.getAccessToken(user, "123", "web-app");
    }

    static final String ADMIN_EMAIL = "admin@gmail.com";

    static final String ORGANIZER_VI_EMAIL = "organizer.vi@gmail.com";

    static final String ORGANIZER_EN_EMAIL = "organizer.en@gmail.com";

    static final String CUSTOMER_VI_EMAIL = "customer.vi@gmail.com";

    static final String CUSTOMER_EN_EMAIL = "customer.en@gmail.com";

    @Test
    void create_withRoleOrganizerAndRegionUS_success() {
        String title = "Test Concert";
        String description = "This is a test concert description";
        String location = "Test Location";
        String region = Region.US.name();
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-01T12:00:00";

        Response response = given()
            .auth().oauth2(getAccessToken(ORGANIZER_EN_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body("""
                {
                    "title": "%s",
                    "description": "%s",
                    "location": "%s",
                    "region": "%s",
                    "startTime": "%s",
                    "endTime": "%s"
                }
                """.formatted(title, description, location, region, startTime, endTime))
            .when().post()
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH,"Create", "concert")))
            .extract().response();

        String concertId = response.jsonPath().getString("data");

        given()
            .auth().oauth2(getAccessToken(ORGANIZER_EN_EMAIL))
            .when().get("/{id}", concertId)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .body("data.id", is(concertId))
            .body("data.region", is(Region.US.name()));

//        given()
//            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
//            .when().delete("/{id}", concertId)
//            .then().log().all()
//            .statusCode(RestResponse.Status.OK.getStatusCode())
//            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH,"Delete", "concert")))
//            .body("data", is(concertId));
    }

}