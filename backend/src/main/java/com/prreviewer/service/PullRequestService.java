package com.prreviewer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.prreviewer.model.PullRequest;
import com.prreviewer.model.PullRequestEvent;
import com.prreviewer.model.PullRequestStatus;
import com.prreviewer.model.Repository;
import com.prreviewer.repository.PullRequestEventRepository;
import com.prreviewer.repository.PullRequestRepository;
import com.prreviewer.repository.RepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handles all business logic for persisting Pull Request metadata received
 * from GitHub webhooks.
 *
 * <h2>Milestone 6 scope</h2>
 * <p>This milestone is deliberately narrow:
 * <ul>
 *   <li>No GitHub API calls — all data comes from the webhook payload.</li>
 *   <li>No diff retrieval.</li>
 *   <li>No AI review.</li>
 *   <li>No comment posting.</li>
 * </ul>
 *
 * <h2>Transaction boundary</h2>
 * <p>{@link #handlePullRequestEvent} is {@code @Transactional}.  The
 * {@link com.prreviewer.webhook.WebhookReceiverService} that calls this method
 * is intentionally <em>not</em> transactional, so that webhook delivery
 * persistence and PR/event persistence are two independent units of work.
 * A failure inside this method does not roll back the already-committed
 * {@link com.prreviewer.model.WebhookDelivery} row, and vice-versa.
 *
 * <h2>Idempotency</h2>
 * <p>This method is safe to call multiple times with the same {@code deliveryId}.
 * A pre-check via {@link PullRequestEventRepository#existsByDeliveryId} provides
 * a fast-path skip; the {@code UNIQUE (delivery_id)} DB constraint is the
 * authoritative guard if two threads race through simultaneously.
 *
 * <h2>Repository lookup</h2>
 * <p>A webhook can only arrive for a repository whose webhook was enabled through
 * our UI (Milestone 4).  Therefore the repository referenced in the payload
 * ({@code repository.id}) must already exist in our database.  If it does not
 * (e.g. the repository was deleted from our side after the webhook was sent),
 * we log a warning and return without creating any records.
 */
@Service
public class PullRequestService {

    private static final Logger log = LoggerFactory.getLogger(PullRequestService.class);

    private final RepositoryRepository       repositoryRepository;
    private final PullRequestRepository      pullRequestRepository;
    private final PullRequestEventRepository eventRepository;

    public PullRequestService(RepositoryRepository repositoryRepository,
                              PullRequestRepository pullRequestRepository,
                              PullRequestEventRepository eventRepository) {
        this.repositoryRepository  = repositoryRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.eventRepository       = eventRepository;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Extracts pull request metadata from a verified webhook payload, then
     * finds-or-creates the parent {@link PullRequest} and appends a new
     * {@link PullRequestEvent}.
     *
     * <p>Flow:
     * <ol>
     *   <li>Idempotency pre-check — skip if {@code deliveryId} already recorded.</li>
     *   <li>Extract {@code githubRepoId}, {@code prNumber}, and all other fields.</li>
     *   <li>Look up the {@link Repository} by {@code githubRepoId}; warn and return
     *       if not found.</li>
     *   <li>Find-or-create the {@link PullRequest} (keyed by {@code repositoryId +
     *       prNumber}).</li>
     *   <li>Update {@code headSha} on the PR (always — handles {@code synchronize}
     *       events).</li>
     *   <li>Build and persist a {@link PullRequestEvent} for this specific delivery.</li>
     * </ol>
     *
     * @param root       the parsed root {@link JsonNode} of the webhook payload
     * @param event      the value of the {@code X-GitHub-Event} header
     * @param action     the {@code action} field from the payload
     * @param deliveryId the value of the {@code X-GitHub-Delivery} header
     * @return an Optional containing the {@link PullRequest} if it was successfully processed,
     *         or empty if it was skipped (e.g. duplicate or unknown repository).
     */
    @Transactional
    public Optional<PullRequest> handlePullRequestEvent(JsonNode root, String event, String action, String deliveryId) {

        // Step 1: idempotency pre-check
        if (eventRepository.existsByDeliveryId(deliveryId)) {
            log.info("PullRequestEvent already recorded — skipping (deliveryId={})", deliveryId);
            return Optional.empty();
        }

        // Step 2: extract fields from payload
        long    githubRepoId    = root.path("repository").path("id").asLong();
        int     prNumber        = root.path("pull_request").path("number").asInt();
        String  title           = root.path("pull_request").path("title").asText("");
        String  body            = root.path("pull_request").path("body").asText(null);
        String  headSha         = root.path("pull_request").path("head").path("sha").asText(null);
        String  nodeId          = root.path("pull_request").path("node_id").asText(null);
        String  senderLogin     = root.path("sender").path("login").asText("unknown");
        Long    installationId  = root.path("installation").path("id").isMissingNode() ||
                                  root.path("installation").path("id").isNull()
                                  ? null
                                  : root.path("installation").path("id").asLong();

        // Step 3: look up Repository by GitHub repo id
        Repository repository = repositoryRepository.findByGithubRepoId(githubRepoId)
                .orElse(null);

        if (repository == null) {
            log.warn("Received pull_request webhook for unknown githubRepoId={} — " +
                     "repository may have been removed. Skipping (deliveryId={}).",
                     githubRepoId, deliveryId);
            return Optional.empty();
        }

        // Step 4: find-or-create PullRequest
        PullRequest pr = pullRequestRepository
                .findByRepositoryIdAndGithubPrNumber(repository.getId(), prNumber)
                .orElseGet(() -> {
                    log.info("Creating new PullRequest: repoId={}, prNumber={}", repository.getId(), prNumber);
                    return pullRequestRepository.save(
                            PullRequest.builder()
                                    .repository(repository)
                                    .githubPrNumber(prNumber)
                                    .title(title)
                                    .description(body)
                                    .status(PullRequestStatus.PENDING)
                                    .headSha(headSha)
                                    .githubPrNodeId(nodeId)
                                    .build()
                    );
                });

        // Step 5: update mutable fields on the PR
        // Title, body, and headSha can change across events (e.g. synchronize pushes a new commit).
        pr.setTitle(title);
        pr.setDescription(body);
        pr.setHeadSha(headSha);
        if (nodeId != null && pr.getGithubPrNodeId() == null) {
            pr.setGithubPrNodeId(nodeId);
        }
        // Dirty-checking will flush the update on transaction commit — no explicit save() needed.

        // Step 6: persist the event record
        try {
            PullRequestEvent prEvent = PullRequestEvent.builder()
                    .pullRequest(pr)
                    .deliveryId(deliveryId)
                    .event(event)
                    .action(action)
                    .senderLogin(senderLogin)
                    .installationId(installationId)
                    .build();
            eventRepository.save(prEvent);

            log.info("Persisted PullRequestEvent: prId={}, action={}, deliveryId={}",
                     pr.getId(), action, deliveryId);

        } catch (DataIntegrityViolationException ex) {
            // Race condition: two threads processed the same delivery simultaneously.
            // The DB unique constraint on delivery_id caught the duplicate — safe to swallow.
            log.warn("Duplicate PullRequestEvent detected by DB constraint — ignoring " +
                     "(deliveryId={}, prId={})", deliveryId, pr.getId());
            return Optional.empty();
        }

        return Optional.of(pr);
    }
}
