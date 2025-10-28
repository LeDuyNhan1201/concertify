package org.tma.intern.common.base;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.config.SmallRyeConfig;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ConfigurationsLogger {

    void onStart(@Observes StartupEvent event, SmallRyeConfig config) {
        log.info("===== Detailed Configuration Sources =====");
        config.getPropertyNames().forEach(name -> {
            String value = config.getRawValue(name);
            String source = config.getConfigValue(name).getConfigSourceName();
            if (name.toLowerCase().contains("password") || name.toLowerCase().contains("secret")) {
                value = "****";
            }
            log.info("{} = {}  [source: {}]", name, value, source);
        });
        log.info("==========================================");
    }

}
