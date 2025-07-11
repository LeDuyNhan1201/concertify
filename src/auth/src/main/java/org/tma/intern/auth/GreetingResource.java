package org.tma.intern.auth;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.tma.intern.common.locale.LocaleProvider;

@Path("/hello")
public class GreetingResource {

    private final LocaleProvider locale;

    public GreetingResource(LocaleProvider locale) {
        this.locale = locale;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return locale.getMessage("greeting", "Ben");
    }

}
