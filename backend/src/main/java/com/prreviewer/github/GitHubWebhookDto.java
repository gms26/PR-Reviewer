package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a single webhook object returned by the GitHub REST API
 * ({@code GET /repos/{owner}/{repo}/hooks}).
 *
 * <p>Only the fields this application uses are declared. All other fields in
 * the GitHub JSON response are silently ignored via
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)}.
 *
 * <p>This is a pure data carrier. Matching logic — deciding whether a webhook
 * belongs to this application — lives in {@link GitHubService}, not here.
 * The DTO knows its {@code config.url()}; it does not know whether that URL
 * matches the current environment's webhook endpoint.
 *
 * @param id     the GitHub-assigned numeric webhook ID; stored as {@code webhook_id}
 *               in the database and used to target DELETE requests
 * @param active whether GitHub considers this webhook active and will deliver events
 * @param events the list of event types this webhook is subscribed to
 *               (e.g. {@code ["pull_request"]}); typed as {@code List<String>}
 *               so {@code events.contains("pull_request")} is straightforward
 * @param config the nested config object containing the webhook URL, content type,
 *               and SSL verification setting; may be {@code null} for malformed
 *               responses — callers should null-check before reading
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubWebhookDto(

        @JsonProperty("id")
        Long id,

        @JsonProperty("active")
        boolean active,

        @JsonProperty("events")
        List<String> events,

        @JsonProperty("config")
        WebhookConfigDto config
) {}
