package org.tma.intern.common.base;

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

    @ConfigProperty(name = "quarkus.tls.ssl/tls.trust-store.p12.path")
    String truststorePath;

    @ConfigProperty(name = "quarkus.tls.ssl/tls.trust-store.p12.password")
    String truststorePassword;

    @Produces
    @Singleton
    public WebClient webClient(Vertx vertx) {
        return WebClient.create(vertx, new WebClientOptions()
            .setSsl(true)
            .setPfxTrustOptions(new PfxOptions()
                .setPath(truststorePath)
                .setPassword(truststorePassword))
            .setVerifyHost(true)
        );
    }

}
