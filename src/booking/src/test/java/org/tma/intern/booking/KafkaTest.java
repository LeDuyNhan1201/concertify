package org.tma.intern.booking;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.SseEventSource;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@QuarkusTest
public class KafkaTest {

    @TestHTTPResource("/hello/consumed")
    URI consumedGreetings;

    @Test
    public void testHelloEndpoint() throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(consumedGreetings);

        List<String> received = new CopyOnWriteArrayList<>();

        SseEventSource source = SseEventSource.target(target).build();
        source.register(inboundSseEvent -> received.add(inboundSseEvent.readData()));

        // in a separate thread, feed the `MovieResource`
        ExecutorService greetingSender = startSendingGreetings();

        source.open();

        // check if, after at most 5 seconds, we have at least 2 items collected, and they are what we expect
        await().atMost(5, SECONDS).until(() -> received.size() >= 2);
        assertThat(received, Matchers.hasItems("Hello, Ben!!!"));
        source.close();

        // shutdown the executor that is feeding the `MovieResource`
        greetingSender.shutdownNow();
        greetingSender.awaitTermination(5, SECONDS);
    }

    private ExecutorService startSendingGreetings() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            while (true) {
                given()
                    .when()
                    .get("/hello")
                    .then()
                    .statusCode(200);

                given()
                    .when()
                    .get("/hello")
                    .then()
                    .statusCode(200);

                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        return executorService;
    }

}
