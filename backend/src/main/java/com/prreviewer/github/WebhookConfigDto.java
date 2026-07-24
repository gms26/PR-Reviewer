package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models the {@code config} sub-object inside a GitHub webhook.
 *
 * <p>Used in two directions:
 * <ul>
 *   <li><strong>Inbound (GET):</strong> deserializes the {@code config} block
 *       from {@code GET /repos/{owner}/{repo}/hooks} responses via
 *       {@link GitHubWebhookDto}.</li>
 *   <li><strong>Outbound (POST):</strong> serializes the {@code config} block
 *       sent in the {@code POST /repos/{owner}/{repo}/hooks} request body via
 *       {@link CreateWebhookRequest}.</li>
 * </ul>
 *
 * <p>Only the fields this application reads or writes are declared.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} silently drops any
 * other fields GitHub includes in the response.
 *
 * <p>This is a pure data carrier — no business logic, no helper methods.
 * URL-matching decisions belong in the service layer.
 *
 * @param url         the URL GitHub will POST webhook payloads to
 * @param contentType the payload format; always {@code "application/json"} for
 *                    this application
 * @param insecureSsl {@code "0"} = SSL verification enabled (required);
 *                    {@code "1"} = SSL verification disabled (never use in production)
 * @param secret      the HMAC-SHA256 signing secret; GitHub never returns this
 *                    field when reading hooks (it is write-only), so this field
 *                    will always be {@code null} on deserialized inbound responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookConfigDto(

        @JsonProperty("url")
        String url,

        @JsonProperty("content_type")
        String contentType,

        @JsonProperty("insecure_ssl")
        String insecureSsl,

        @JsonProperty("secret")
        String secret
) {}
