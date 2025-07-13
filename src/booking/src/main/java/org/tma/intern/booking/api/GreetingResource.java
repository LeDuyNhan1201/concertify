package org.tma.intern.booking.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.tma.intern.common.locale.LocaleProvider;

@Path("/hello")
public class GreetingResource {

    private final LocaleProvider locale;

    @Inject
    @Channel("greeting-out")
    private Emitter<String> greetingEmitter;

    public GreetingResource(LocaleProvider locale) {
        this.locale = locale;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return greetingEmitter.send(locale.getMessage("greeting", "Ben"))
                .thenApply(ignored -> "Greeting sent successfully!")
                .toCompletableFuture()
                .join();
    }

}
