package com.prreviewer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs a structured startup summary once the application is fully started
 * and ready to serve traffic.
 *
 * <p>Listening on {@link ApplicationReadyEvent} (not {@code ContextRefreshedEvent})
 * ensures this fires after the embedded web server is bound to a port, Flyway
 * migrations have run, and the HikariCP pool has been validated — meaning the
 * log line is a reliable signal that the application is truly operational.
 *
 * <p>What is logged:
 * <ul>
 *   <li>Active Spring profiles</li>
 *   <li>Bound port</li>
 *   <li>Database URL (host only, never password)</li>
 * </ul>
 *
 * <p>What is never logged:
 * <ul>
 *   <li>Database credentials</li>
 *   <li>GitHub OAuth secrets</li>
 *   <li>Gemini API key</li>
 *   <li>Webhook secret</li>
 * </ul>
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupListener.class);

    private final Environment environment;

    public ApplicationStartupListener(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length > 0
            ? String.join(", ", activeProfiles)
            : "default";

        String port = environment.getProperty("server.port", "8080");

        // Extract only the host portion of the DB URL — never log credentials
        String dbUrl    = environment.getProperty("spring.datasource.url", "");
        String dbSummary = sanitizeDatabaseUrl(dbUrl);

        log.info("================================================");
        log.info("  PR Reviewer started successfully");
        log.info("  Profile(s) : {}", profiles);
        log.info("  Port       : {}", port);
        log.info("  Database   : {}", dbSummary);
        log.info("  Health     : http://localhost:{}/health", port);
        log.info("================================================");
    }

    /**
     * Returns only the host and database name portion of a JDBC URL,
     * stripping any embedded credentials or sensitive query parameters.
     *
     * <p>Example:
     * {@code jdbc:postgresql://ep-xxx.neon.tech/pr_reviewer?sslmode=require}
     * → {@code postgresql://ep-xxx.neon.tech/pr_reviewer}
     */
    private String sanitizeDatabaseUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return "(not configured)";
        }
        // Strip the "jdbc:" prefix
        String url = jdbcUrl.startsWith("jdbc:") ? jdbcUrl.substring(5) : jdbcUrl;
        // Strip query parameters
        int queryIdx = url.indexOf('?');
        if (queryIdx > 0) {
            url = url.substring(0, queryIdx);
        }
        // Strip embedded user:password@ if present
        int atIdx = url.indexOf('@');
        if (atIdx > 0) {
            int protocolEnd = url.indexOf("://");
            if (protocolEnd > 0) {
                url = url.substring(0, protocolEnd + 3) + url.substring(atIdx + 1);
            }
        }
        return url;
    }
}
