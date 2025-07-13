package org.tma.intern.common.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import net.datafaker.Faker;

@ApplicationScoped
public class FakerProducer {

    @Produces
    @Singleton
    public Faker faker() {
        return new Faker();
    }

}
