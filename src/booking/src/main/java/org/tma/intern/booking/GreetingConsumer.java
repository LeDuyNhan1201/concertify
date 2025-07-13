package org.tma.intern.booking;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@Slf4j
public class GreetingConsumer {

    @Incoming("greeting-in")
    public void receiveGreeting(String message) {
        log.warn("Received: {}", message);
    }
}
