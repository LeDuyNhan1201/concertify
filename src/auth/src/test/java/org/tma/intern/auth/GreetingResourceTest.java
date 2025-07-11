package org.tma.intern.auth;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.tma.intern.auth.api.GreetingResource;
import org.tma.intern.common.helper.StringHelper;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@TestHTTPEndpoint(GreetingResource.class)
@QuarkusTest
class GreetingResourceTest {

    @Test
    void testHelloEndpoint() {
        given()
            .header("Accept-Language", "en")
            .when().get("")
            .then().log().all()
            .statusCode(200)
            .body(is(StringHelper.getMessage("greeting", Locale.ENGLISH, "Ben")));
    }

}