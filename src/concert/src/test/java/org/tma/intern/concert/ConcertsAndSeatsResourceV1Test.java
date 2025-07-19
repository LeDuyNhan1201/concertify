package org.tma.intern.concert;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.common.mapper.TypeRef;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.*;
import org.tma.intern.common.dto.CommonResponse;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.Region;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
class ConcertsAndSeatsResourceV1Test {

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

    static final String CONCERTS_PATH = "/v1/concerts";

    static final String ORGANIZER_CONCERTS_PATH = "/v1/organizer/concerts";

    static final String SEATS_PATH = "/v1/seats";

    String concertId;

    List<String> seatIds = new ArrayList<>();

    @Order(1)
    @Test
    void create_withOrganizerInUS_success() {
        ConcertRequest.Info request = new ConcertRequest.Info(
            "Test Concert",
            "Ho Chi Minh City",
            LocalDateTime.of(2023, 1, 11, 23, 0),
            LocalDateTime.of(2023, 1, 12, 10, 0)
        );

        CommonResponse<String> response = given()
            .auth().oauth2(getAccessToken(ORGANIZER_US_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body(request)
            .when().post(ORGANIZER_CONCERTS_PATH)
            .then().log().all()
            .statusCode(RestResponse.Status.CREATED.getStatusCode())
            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH, "Create", "concert")))
            .extract()
            .as(new TypeRef<>() {}); // response data là concertId dạng String

        concertId = response.getData();
//        given()
//            .auth().oauth2(getAccessToken(ADMIN_EMAIL))
//            .when().delete("/{id}", concertId)
//            .then().log().all()
//            .statusCode(RestResponse.Status.OK.getStatusCode())
//            .body("message", is(StringHelper.getMessage("Action.Success", Locale.ENGLISH,"Delete", "concert")))
//            .body("data", is(concertId));
    }

    @Order(2)
    @Test
    void getPreviewDetails_withOrganizerInUS_success() {
        CommonResponse<ConcertResponse.DetailsWithSeats> response = given()
            .auth().oauth2(getAccessToken(ORGANIZER_US_EMAIL))
            .when().get(ORGANIZER_CONCERTS_PATH + "/{id}", concertId)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .extract().as(new TypeRef<>() {
            });

        ConcertResponse.DetailsWithSeats detailsConcert = response.getData();
        int seatsCount = detailsConcert.getSeats().size();
        Assertions.assertEquals(concertId, detailsConcert.getId());
        Assertions.assertEquals(Region.US, detailsConcert.getRegion());
        Assertions.assertEquals(120.0, detailsConcert.getSeats().get(0).getPrice());
        Assertions.assertEquals(120.0, detailsConcert.getSeats().get(1).getPrice());
        Assertions.assertEquals(60.0, detailsConcert.getSeats().get(seatsCount - 2).getPrice());
        Assertions.assertEquals(60.0, detailsConcert.getSeats().get(seatsCount - 2).getPrice());

        seatIds.add(detailsConcert.getSeats().get(0).getId());
        seatIds.add(detailsConcert.getSeats().get(1).getId());
        seatIds.add(detailsConcert.getSeats().get(seatsCount - 2).getId());
        seatIds.add(detailsConcert.getSeats().get(seatsCount - 1).getId());
    }

    @Order(3)
    @Test
    void holdSeats_withCustomerInUS_success() {
        given()
            .auth().oauth2(getAccessToken(CUSTOMER_US_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body(new ConcertRequest.SeatIds(seatIds))
            .when().put(SEATS_PATH + "/hold/{id}/concert", concertId)
            .then().log().all()
            .statusCode(RestResponse.Status.OK.getStatusCode())
            .extract().as(new TypeRef<>() {
            });
    }

    @Order(4)
    @Test
    void holdSeats_withCustomerInUS_conflict() {
        given()
            .auth().oauth2(getAccessToken(CUSTOMER_US_EMAIL))
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .body(new ConcertRequest.SeatIds(seatIds))
            .when().put(SEATS_PATH + "/hold/{id}/concert", concertId)
            .then().log().all()
            .statusCode(RestResponse.Status.CONFLICT.getStatusCode())
            .extract().as(new TypeRef<>() {
            });
    }

}