package com.prreviewer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Factory configuration for RestClient instances.
 *
 * <p>Two pre-configured clients are provided:
 * <ul>
 *   <li>{@code githubRestClient} — GitHub API v3 client with Accept header set to
 *       the GitHub JSON media type and explicit connect/read timeouts.</li>
 *   <li>{@code geminiRestClient} — Gemini API client base; auth is appended per
 *       request via query param so the key is never stored in headers.</li>
 * </ul>
 *
 * <h2>Timeout policy</h2>
 * <p>Both clients use {@link SimpleClientHttpRequestFactory} with:
 * <ul>
 *   <li><strong>Connect timeout: 10 seconds</strong> — fail fast if the remote
 *       host is unreachable. Prevents indefinite thread blocking during outages.</li>
 *   <li><strong>Read timeout: 30 seconds</strong> — allows paginated responses
 *       and large payloads sufficient time to arrive, while still bounding the
 *       worst-case wait for a slow but connected host.</li>
 * </ul>
 * <p>{@code SimpleClientHttpRequestFactory} is included with {@code spring-web};
 * no additional dependency is required.
 */
@Configuration
public class RestClientConfig {

    /** Connect timeout in milliseconds applied to all GitHub API requests. */
    private static final int GITHUB_CONNECT_TIMEOUT_MS = 10_000;

    /** Read timeout in milliseconds applied to all GitHub API requests. */
    private static final int GITHUB_READ_TIMEOUT_MS = 30_000;

    /** Connect timeout in milliseconds applied to all Gemini API requests. */
    private static final int GEMINI_CONNECT_TIMEOUT_MS = 10_000;

    /** Read timeout in milliseconds applied to all Gemini API requests.
     *  Gemini may take longer to generate a response than GitHub takes to
     *  return data, so this is intentionally generous. */
    private static final int GEMINI_READ_TIMEOUT_MS = 60_000;

    @Value("${app.github.api-base-url}")
    private String githubApiBaseUrl;

    @Value("${app.gemini.api-url}")
    private String geminiApiUrl;

    @Bean("githubRestClient")
    public RestClient githubRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(GITHUB_CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(GITHUB_READ_TIMEOUT_MS);

        return RestClient.builder()
                .baseUrl(githubApiBaseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    @Bean("geminiRestClient")
    public RestClient geminiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(GEMINI_CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(GEMINI_READ_TIMEOUT_MS);

        return RestClient.builder()
                .baseUrl(geminiApiUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

