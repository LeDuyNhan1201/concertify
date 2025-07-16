package org.tma.intern.booking.api;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.tma.intern.common.contract.Greeting;
import org.tma.intern.common.locale.LocaleProvider;

@Path("/hello")
public class GreetingResource {

    private final LocaleProvider locale;

    @Inject
    @Channel("greeting-out")
    private Emitter<Greeting> greetingEmitter;

    @Channel("greeting-in")
    Multi<Greeting> greetings;

    public GreetingResource(LocaleProvider locale) {
        this.locale = locale;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return greetingEmitter.send(Greeting.newBuilder().setMessage(locale.getMessage("greeting", "Ben")).build())
                .thenApply(ignored -> "Greeting sent successfully!")
                .toCompletableFuture()
                .join();
    }

    @Path("consumed")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> stream() {
        return greetings.map(greeting -> String.format("%s", greeting.getMessage()));
    }

}
