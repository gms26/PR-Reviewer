package com.prreviewer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Component
public class PropertyLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(PropertyLogger.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        
        log.info("=================================================");
        log.info("=== ACTUATOR PROPERTY DIAGNOSTICS START      ===");
        log.info("=================================================");
        
        String[] propertiesToLog = {
            "management.endpoints.web.exposure.include",
            "management.endpoints.web.exposure.exclude",
            "management.endpoints.enabled-by-default",
            "management.endpoint.health.enabled",
            "management.server.port",
            "server.port"
        };
        
        if (env instanceof ConfigurableEnvironment configEnv) {
            for (String propName : propertiesToLog) {
                String resolvedValue = configEnv.getProperty(propName);
                log.info("Property: {} = '{}'", propName, resolvedValue);
                
                boolean found = false;
                for (PropertySource<?> source : configEnv.getPropertySources()) {
                    if (source.containsProperty(propName)) {
                        Object sourceValue = source.getProperty(propName);
                        log.info("   -> Winner PropertySource : '{}'", source.getName());
                        log.info("   -> Source Value          : '{}'", sourceValue);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    log.info("   -> Not explicitly provided by any PropertySource.");
                }
                log.info("-------------------------------------------------");
            }
        }
        
        log.info("=== ACTUATOR PROPERTY DIAGNOSTICS END        ===");
        log.info("=================================================");
    }
}
