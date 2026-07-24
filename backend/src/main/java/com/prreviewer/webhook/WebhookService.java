package com.prreviewer.webhook;

import com.prreviewer.config.AppProperties;
import com.prreviewer.dto.RepositoryResponse;
import com.prreviewer.exception.RepositoryNotFoundException;
import com.prreviewer.exception.RepositoryOwnershipException;
import com.prreviewer.github.GitHubService;
import com.prreviewer.github.GitHubWebhookDto;
import com.prreviewer.model.Repository;
import com.prreviewer.model.User;
import com.prreviewer.repository.RepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Orchestrates GitHub webhook lifecycle for monitored repositories.
 *
 * <p>This service owns all business logic related to enabling and disabling
 * GitHub webhook monitoring. GitHub REST API details are entirely delegated
 * to {@link GitHubService}.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Load repository from database and verify ownership before any mutation</li>
 *   <li>Apply idempotency guard to avoid redundant GitHub API calls</li>
 *   <li>Detect existing webhooks and reuse them (avoid duplicates)</li>
 *   <li>Create new webhooks when none exist</li>
 *   <li>Delete webhooks and clear database state on disable</li>
 *   <li>Keep {@code webhookEnabled} and {@code webhookId} consistent in the database</li>
 * </ul>
 *
 * <h2>What this service does NOT do</h2>
 * <p>GitHub REST API calls, webhook URL construction details, HTTP error mapping —
 * all delegated to {@link GitHubService}.
 *
 * <h2>Ownership policy</h2>
 * <p>Ownership is verified before any GitHub mutation. If
 * {@code repository.user.id != authenticatedUser.id}, a
 * {@link RepositoryOwnershipException} is thrown immediately and GitHub is
 * never contacted.
 *
 * <h2>Transaction policy</h2>
 * <p>Both public methods are {@code @Transactional}. The repository entity is
 * managed within the transaction boundary — Hibernate's dirty-checking mechanism
 * flushes changes automatically on commit, so no explicit {@code save()} call is
 * required after modifying the entity.
 *
 * <h2>Error propagation</h2>
 * <p>{@link com.prreviewer.exception.GitHubApiException},
 * {@link com.prreviewer.exception.GitHubRateLimitException}, and
 * {@link RepositoryOwnershipException} are not caught here — they propagate to
 * {@link com.prreviewer.exception.GlobalExceptionHandler} for consistent HTTP
 * response mapping. Only {@code GitHubService} handles the GitHub 404-on-delete
 * case internally (treating it as idempotent success).
 */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    /** Path appended to app.base-url to form the webhook callback URL. */
    private static final String WEBHOOK_PATH = "/webhook/github";

    private final RepositoryRepository repositoryRepository;
    private final GitHubService        githubService;
    private final AppProperties        appProperties;

    public WebhookService(RepositoryRepository repositoryRepository,
                          GitHubService githubService,
                          AppProperties appProperties) {
        this.repositoryRepository = repositoryRepository;
        this.githubService        = githubService;
        this.appProperties        = appProperties;
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Enables GitHub webhook monitoring for the given repository.
     *
     * <p>Flow:
     * <ol>
     *   <li>Load repository — 404 if not found.</li>
     *   <li>Verify ownership — 403 if mismatch; no GitHub call made.</li>
     *   <li>Idempotency guard — if already monitoring, return immediately.</li>
     *   <li>Check GitHub for an existing webhook matching this application's URL
     *       (exact URL + active + pull_request event).</li>
     *   <li>If found: persist its ID. If not: create a new webhook.</li>
     *   <li>Persist {@code webhookId} and {@code webhookEnabled=true}.</li>
     * </ol>
     *
     * @param authenticatedUser the currently authenticated user
     * @param repositoryId      the internal database ID of the repository to monitor
     * @return the updated repository state
     * @throws RepositoryNotFoundException    if the repository does not exist
     * @throws RepositoryOwnershipException   if the repository belongs to another user
     * @throws com.prreviewer.exception.GitHubRateLimitException if GitHub rate-limits the request
     * @throws com.prreviewer.exception.GitHubApiException       for other GitHub API failures
     */
    @Transactional
    public RepositoryResponse enableMonitoring(User authenticatedUser, Long repositoryId) {
        Repository repo = loadAndVerify(authenticatedUser, repositoryId);

        // Step 3: idempotency — already monitoring, skip all GitHub calls
        if (Boolean.TRUE.equals(repo.getWebhookEnabled()) && repo.getWebhookId() != null) {
            log.info("Monitoring already enabled: repositoryId={}, githubRepoId={}, webhookId={}",
                    repo.getId(), repo.getGithubRepoId(), repo.getWebhookId());
            return RepositoryResponse.from(repo);
        }

        String webhookUrl = buildWebhookUrl();
        String accessToken = repo.getUser().getAccessToken();

        // Step 4-5: check for an existing webhook matching this application's URL
        Optional<GitHubWebhookDto> existing = githubService.findWebhookByUrl(
                accessToken, repo.getOwner(), repo.getName(), webhookUrl);

        // Step 6-7: reuse existing or create new
        long webhookId = existing
                .map(GitHubWebhookDto::id)
                .orElseGet(() -> githubService.createWebhook(
                        accessToken, repo.getOwner(), repo.getName(),
                        webhookUrl, appProperties.getGithubWebhookSecret()).id());

        // Persist — entity is managed within @Transactional; dirty-checking
        // flushes changes on commit automatically.
        repo.setWebhookId(webhookId);
        repo.setWebhookEnabled(true);

        log.info("Monitoring enabled: repositoryId={}, githubRepoId={}, webhookId={}",
                repo.getId(), repo.getGithubRepoId(), webhookId);

        return RepositoryResponse.from(repo);
    }

    /**
     * Disables GitHub webhook monitoring for the given repository.
     *
     * <p>Flow:
     * <ol>
     *   <li>Load repository — 404 if not found.</li>
     *   <li>Verify ownership — 403 if mismatch; no GitHub call made.</li>
     *   <li>Idempotency guard — if already disabled, return immediately.</li>
     *   <li>Delete webhook via {@link GitHubService} — 404 from GitHub is treated
     *       as success (already deleted) by GitHubService; no special handling needed here.</li>
     *   <li>Persist {@code webhookEnabled=false} and {@code webhookId=null}.</li>
     * </ol>
     *
     * @param authenticatedUser the currently authenticated user
     * @param repositoryId      the internal database ID of the repository to stop monitoring
     * @return the updated repository state
     * @throws RepositoryNotFoundException    if the repository does not exist
     * @throws RepositoryOwnershipException   if the repository belongs to another user
     * @throws com.prreviewer.exception.GitHubRateLimitException if GitHub rate-limits the request
     * @throws com.prreviewer.exception.GitHubApiException       for other non-404 GitHub API failures
     */
    @Transactional
    public RepositoryResponse disableMonitoring(User authenticatedUser, Long repositoryId) {
        Repository repo = loadAndVerify(authenticatedUser, repositoryId);

        // Step 3: idempotency — already disabled, nothing to do
        if (!Boolean.TRUE.equals(repo.getWebhookEnabled())) {
            log.info("Monitoring already disabled: repositoryId={}, githubRepoId={}",
                    repo.getId(), repo.getGithubRepoId());
            return RepositoryResponse.from(repo);
        }

        // Step 4: delete webhook from GitHub
        // webhookId must be non-null here (invariant: webhookEnabled=true implies webhookId != null)
        // GitHubService handles 404 internally (idempotent delete).
        githubService.deleteWebhook(
                repo.getUser().getAccessToken(),
                repo.getOwner(),
                repo.getName(),
                repo.getWebhookId());

        // Step 5: clear monitoring state
        repo.setWebhookEnabled(false);
        repo.setWebhookId(null);

        log.info("Monitoring disabled: repositoryId={}, githubRepoId={}",
                repo.getId(), repo.getGithubRepoId());

        return RepositoryResponse.from(repo);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Loads the repository and verifies ownership in one step.
     * Extracted to avoid duplicating the load+verify sequence in both public methods.
     *
     * @throws RepositoryNotFoundException  if the repository does not exist
     * @throws RepositoryOwnershipException if the repository belongs to another user
     */
    private Repository loadAndVerify(User authenticatedUser, Long repositoryId) {
        Repository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new RepositoryNotFoundException(repositoryId));
        verifyOwnership(repo, authenticatedUser);
        return repo;
    }

    /**
     * Verifies that the repository belongs to the authenticated user.
     *
     * <p>Compares internal database user IDs — not GitHub IDs. Called before
     * any GitHub mutation to ensure users can only manage their own repositories.
     *
     * @throws RepositoryOwnershipException if the IDs do not match
     */
    private void verifyOwnership(Repository repo, User authenticatedUser) {
        if (!repo.getUser().getId().equals(authenticatedUser.getId())) {
            throw new RepositoryOwnershipException(
                    "User " + authenticatedUser.getId()
                    + " does not own repository " + repo.getId());
        }
    }

    /**
     * Constructs the full webhook callback URL for this application.
     * GitHub will POST pull_request event payloads to this URL.
     *
     * @return e.g. {@code "https://pr-reviewer.onrender.com/webhook/github"}
     */
    private String buildWebhookUrl() {
        return appProperties.getBaseUrl() + WEBHOOK_PATH;
    }
}
