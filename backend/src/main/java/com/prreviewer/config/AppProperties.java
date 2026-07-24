package com.prreviewer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Strongly-typed application properties.
 *
 * <p>Centralizes all custom {@code app.*} configuration so that services
 * depend on this class rather than scattered {@code @Value} annotations.
 * Each service receives only the properties it needs via constructor injection.
 */
@Configuration
public class AppProperties {

    // Application
    @Value("${app.base-url}")
    private String baseUrl;

    // GitHub
    @Value("${app.github.webhook-secret}")
    private String githubWebhookSecret;

    @Value("${app.github.api-base-url}")
    private String githubApiBaseUrl;

    // Gemini
    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.gemini.api-url}")
    private String geminiApiUrl;

    @Value("${app.gemini.max-retries}")
    private int geminiMaxRetries;

    // Frontend
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public String getBaseUrl()             { return baseUrl; }
    public String getGithubWebhookSecret() { return githubWebhookSecret; }
    public String getGithubApiBaseUrl()    { return githubApiBaseUrl; }
    public String getGeminiApiKey()        { return geminiApiKey; }
    public String getGeminiApiUrl()        { return geminiApiUrl; }
    public int    getGeminiMaxRetries()    { return geminiMaxRetries; }
    public String getFrontendUrl()         { return frontendUrl; }
}
