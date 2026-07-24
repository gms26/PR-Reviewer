package com.prreviewer.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Fail-fast startup validator.
 *
 * <p>Spring will bind a missing required env var as the literal string
 * "${VAR_NAME}" or null in some configurations, rather than failing loudly.
 * This validator checks all security-critical properties at startup and throws
 * an {@link IllegalStateException} before the application accepts any traffic,
 * giving a clear error message instead of subtle runtime failures.
 *
 * <p>Validated variables:
 * <ul>
 *   <li>GITHUB_CLIENT_ID — required for OAuth login</li>
 *   <li>GITHUB_CLIENT_SECRET — required for OAuth token exchange</li>
 *   <li>GITHUB_WEBHOOK_SECRET — required for signature verification</li>
 *   <li>GEMINI_API_KEY — required for AI review</li>
 *   <li>DATABASE_URL — required for persistence</li>
 * </ul>
 */
@Component
public class StartupValidator {

    private static final Logger log = LoggerFactory.getLogger(StartupValidator.class);

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    @Value("${app.github.webhook-secret}")
    private String webhookSecret;

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @PostConstruct
    public void validate() {
        List<String> missing = new ArrayList<>();

        if (isBlankOrUnresolved(githubClientId))     missing.add("GITHUB_CLIENT_ID");
        if (isBlankOrUnresolved(githubClientSecret)) missing.add("GITHUB_CLIENT_SECRET");
        if (isBlankOrUnresolved(webhookSecret))      missing.add("GITHUB_WEBHOOK_SECRET");
        if (isBlankOrUnresolved(geminiApiKey))       missing.add("GEMINI_API_KEY");
        if (isBlankOrUnresolved(databaseUrl))        missing.add("DATABASE_URL");

        if (!missing.isEmpty()) {
            String msg = "Application startup failed. The following required environment " +
                         "variables are missing or blank: " + missing;
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        log.info("StartupValidator: all required environment variables are present.");
    }

    /**
     * Returns true if the value is null, blank, or still contains the
     * unresolved Spring placeholder syntax (e.g., "${SOME_VAR}").
     */
    private boolean isBlankOrUnresolved(String value) {
        return value == null
            || value.isBlank()
            || (value.startsWith("${") && value.endsWith("}"));
    }
}
