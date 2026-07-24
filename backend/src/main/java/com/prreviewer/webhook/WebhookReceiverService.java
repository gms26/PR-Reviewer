package com.prreviewer.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prreviewer.model.WebhookDelivery;
import com.prreviewer.repository.WebhookDeliveryRepository;
import com.prreviewer.service.PullRequestService;
import com.prreviewer.github.GitHubPullRequestService;
import com.prreviewer.model.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Core business logic for receiving and processing GitHub webhooks.
 *
 * <p>Implements the flow decided across Milestones 5 and 6:
 * <ol>
 *   <li>Verify HMAC signature (rejects unauthorized immediately)</li>
 *   <li>Parse JSON</li>
 *   <li>Event filter (silently ignores non-{@code pull_request})</li>
 *   <li>Action filter (silently ignores non-{@code opened}/{@code synchronize})</li>
 *   <li>Duplicate check (silently ignores retries)</li>
 *   <li>Persist {@link com.prreviewer.model.WebhookDelivery} — <strong>committed
 *       independently</strong> of step 7</li>
 *   <li>Delegate to {@link PullRequestService#handlePullRequestEvent} —
 *       runs in its own {@code @Transactional} boundary</li>
 * </ol>
 *
 * <h2>Transaction policy</h2>
 * <p>This service is intentionally <strong>not</strong> {@code @Transactional}.
 * {@link com.prreviewer.model.WebhookDelivery} persistence (step 6) and
 * {@link com.prreviewer.model.PullRequestEvent} persistence (step 7) are
 * independent units of work:
 * <ul>
 *   <li>A failure in step 7 does <em>not</em> roll back the delivery row.</li>
 *   <li>A duplicate delivery that reaches step 5 is caught before step 6 is
 *       re-executed.</li>
 * </ul>
 */
@Service
public class WebhookReceiverService {

    private static final Logger log = LoggerFactory.getLogger(WebhookReceiverService.class);

    private final WebhookSignatureVerifier  signatureVerifier;
    private final WebhookDeliveryRepository deliveryRepository;
    private final ObjectMapper              objectMapper;
    private final PullRequestService        pullRequestService;
    private final GitHubPullRequestService  gitHubPullRequestService;

    public WebhookReceiverService(WebhookSignatureVerifier signatureVerifier,
                                  WebhookDeliveryRepository deliveryRepository,
                                  ObjectMapper objectMapper,
                                  PullRequestService pullRequestService,
                                  GitHubPullRequestService gitHubPullRequestService) {
        this.signatureVerifier        = signatureVerifier;
        this.deliveryRepository       = deliveryRepository;
        this.objectMapper             = objectMapper;
        this.pullRequestService       = pullRequestService;
        this.gitHubPullRequestService = gitHubPullRequestService;
    }

    /**
     * Processes an incoming webhook delivery.
     *
     * <p>All successful paths (including ignored events and duplicates) return normally,
     * signaling to the controller to return HTTP 200. Only signature verification failures
     * or internal errors throw exceptions.
     *
     * @param rawPayload      the raw HTTP body
     * @param signatureHeader the X-Hub-Signature-256 header
     * @param event           the X-GitHub-Event header
     * @param deliveryId      the X-GitHub-Delivery header
     */
    public void processWebhook(String rawPayload, String signatureHeader, String event, String deliveryId) {
        // 1. Verify HMAC
        signatureVerifier.verify(rawPayload, signatureHeader);

        // 2. Parse JSON to read the action
        JsonNode root;
        try {
            root = objectMapper.readTree(rawPayload);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse webhook payload for deliveryId={}", deliveryId, e);
            // Treat unparseable payloads as processed to prevent GitHub from endlessly retrying garbage
            return;
        }

        String action = root.path("action").asText(null);
        if (action == null) {
            action = "unknown";
        }

        // 3. Event == pull_request ?
        if (!"pull_request".equals(event)) {
            log.debug("Ignoring unsupported event '{}' (deliveryId={})", event, deliveryId);
            return;
        }

        // 4. Action == opened/synchronize ?
        if (!"opened".equals(action) && !"synchronize".equals(action)) {
            log.debug("Ignoring unsupported pull_request action '{}' (deliveryId={})", action, deliveryId);
            return;
        }

        // 5. Already processed ?
        if (deliveryRepository.existsById(deliveryId)) {
            log.info("Duplicate delivery detected (already processed). Ignoring deliveryId={}", deliveryId);
            return;
        }

        // 6. Persist delivery (own implicit transaction — auto-committed by Spring)
        WebhookDelivery delivery = new WebhookDelivery(deliveryId, event, action, rawPayload);
        deliveryRepository.save(delivery);

        log.info("Successfully persisted new pull_request {} event (deliveryId={})", action, deliveryId);

        // 7. Extract PR metadata and persist PullRequest + PullRequestEvent.
        // Runs in its own @Transactional boundary inside PullRequestService.
        // A failure here does not roll back the WebhookDelivery row above.
        Optional<PullRequest> processedPr = pullRequestService.handlePullRequestEvent(root, event, action, deliveryId);

        // 8. Fetch detailed data from GitHub (Milestone 7).
        // Only run if the PR was successfully processed (not skipped as duplicate).
        processedPr.ifPresent(pr -> {
            log.debug("Initiating GitHub data fetch for PR id={} (repo={}/{})", 
                      pr.getId(), pr.getRepository().getOwner(), pr.getRepository().getName());
            gitHubPullRequestService.fetchAllPullRequestData(
                    pr.getRepository().getUser(),
                    pr.getRepository(),
                    pr.getGithubPrNumber()
            );
        });
    }
}
