package org.tma.intern.common.config;

import io.quarkus.arc.profile.IfBuildProfile;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class WebClientProducer {

    @IfBuildProfile("dev")
    @ConfigProperty(name = "%dev.quarkus.tls.ssl-tls.trust-store.p12.path", defaultValue = "")
    String truststorePath;

    @IfBuildProfile("dev")
    @ConfigProperty(name = "%dev.quarkus.tls.ssl-tls.trust-store.p12.password", defaultValue = "")
    String truststorePassword;

    @Produces
    @Singleton
    @IfBuildProfile("dev")
    public WebClient authRestClient(Vertx vertx) {
        return WebClient.create(vertx, new WebClientOptions()
            .setSsl(true)
            .setPfxTrustOptions(new PfxOptions()
                .setPath(truststorePath)
                .setPassword(truststorePassword))
            .setVerifyHost(true)
        );
    }

    @Produces
    @Singleton
    @IfBuildProfile("test")
    public WebClient testAuthRestClient(Vertx vertx) {
        return WebClient.create(vertx, new WebClientOptions());
    }

}
