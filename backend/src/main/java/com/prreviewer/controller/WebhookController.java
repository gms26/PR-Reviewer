package com.prreviewer.controller;

import com.prreviewer.webhook.WebhookReceiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint for receiving webhooks from GitHub.
 *
 * <p>This controller is completely public (permitted in SecurityConfig).
 * Authentication is provided solely by the {@code X-Hub-Signature-256} header,
 * which is verified by {@link WebhookReceiverService}.
 *
 * <p>The controller design strictly enforces:
 * <ul>
 *   <li><strong>Raw Payload:</strong> The body is received as a raw {@code String}
 *       so the exact HMAC-SHA256 signature can be computed before Jackson modifies it.</li>
 *   <li><strong>Thin Layer:</strong> All validation, filtering, and persistence
 *       is delegated to the service layer.</li>
 *   <li><strong>Graceful Responses:</strong> Returns HTTP 200 OK for all valid payloads,
 *       even if they are duplicates or unsupported events, as recommended by GitHub.</li>
 * </ul>
 */
@RestController
@RequestMapping("/webhook/github")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookReceiverService webhookReceiverService;

    public WebhookController(WebhookReceiverService webhookReceiverService) {
        this.webhookReceiverService = webhookReceiverService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader,
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId) {

        log.debug("Received GitHub webhook: event={}, deliveryId={}", event, deliveryId);

        webhookReceiverService.processWebhook(rawPayload, signatureHeader, event, deliveryId);

        return ResponseEntity.ok().build();
    }
}
