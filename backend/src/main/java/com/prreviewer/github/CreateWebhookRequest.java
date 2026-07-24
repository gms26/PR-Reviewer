package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request body for {@code POST /repos/{owner}/{repo}/hooks}.
 *
 * <p>Serialized to JSON by Jackson and sent to GitHub to create a new webhook.
 * Immutable by construction — built once in {@link GitHubService} and sent
 * directly; no intermediate mutation possible.
 *
 * <p>GitHub API field notes:
 * <ul>
 *   <li>{@code name} — must be {@code "web"} for all HTTP webhooks. This is
 *       a GitHub API requirement, not a human-readable label.</li>
 *   <li>{@code config} — the nested {@link WebhookConfigDto} containing URL,
 *       content type, SSL setting, and the HMAC signing secret.</li>
 *   <li>{@code events} — the list of GitHub event types to subscribe to.
 *       For this application: {@code ["pull_request"]}.</li>
 *   <li>{@code active} — {@code true} so GitHub starts delivering payloads
 *       immediately after creation.</li>
 * </ul>
 *
 * @param name   always {@code "web"} — required by the GitHub API for HTTP webhooks
 * @param config the webhook configuration (URL, content type, SSL, secret)
 * @param events the GitHub event types to subscribe to
 * @param active whether GitHub should immediately start delivering payloads
 */
public record CreateWebhookRequest(

        @JsonProperty("name")
        String name,

        @JsonProperty("config")
        WebhookConfigDto config,

        @JsonProperty("events")
        List<String> events,

        @JsonProperty("active")
        boolean active
) {}
