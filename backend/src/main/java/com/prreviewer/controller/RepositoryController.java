package com.prreviewer.controller;

import com.prreviewer.auth.CurrentUserService;
import com.prreviewer.dto.RepositoryResponse;
import com.prreviewer.dto.SelectRepositoryRequest;
import com.prreviewer.model.User;
import com.prreviewer.service.RepositoryService;
import com.prreviewer.webhook.WebhookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for repository management and webhook monitoring.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET  /repos}                   — list all accessible GitHub repositories</li>
 *   <li>{@code POST /repos/select}             — select a repository for PR tracking</li>
 *   <li>{@code POST /repos/{repositoryId}/enable}  — enable webhook monitoring</li>
 *   <li>{@code POST /repos/{repositoryId}/disable} — disable webhook monitoring</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <p>This controller is intentionally thin. It only:
 * <ol>
 *   <li>Resolves the authenticated user via {@link CurrentUserService}.</li>
 *   <li>Validates path variables and request bodies (Bean Validation).</li>
 *   <li>Delegates to {@link RepositoryService} or {@link WebhookService}.</li>
 *   <li>Returns the DTO from the service as the HTTP response.</li>
 * </ol>
 *
 * <p>It does NOT contain: ownership logic, GitHub API calls, webhook logic,
 * persistence, or business rules. All of those live in the service layer.
 *
 * <h2>Authentication</h2>
 * <p>Every endpoint receives the authenticated {@link OAuth2User} principal via
 * {@code @AuthenticationPrincipal}. {@link CurrentUserService#resolve(OAuth2User)}
 * converts it to a database-backed {@link User} entity — the same mechanism used
 * in {@link com.prreviewer.auth.AuthController}. Unauthenticated requests are
 * intercepted by Spring Security before reaching this controller (HTTP 401).
 *
 * <h2>Error handling</h2>
 * <p>Business exceptions propagate to
 * {@link com.prreviewer.exception.GlobalExceptionHandler}:
 * <ul>
 *   <li>{@code RepositoryNotFoundException}  → 404</li>
 *   <li>{@code RepositoryOwnershipException} → 403</li>
 *   <li>{@code GitHubRateLimitException}     → 503</li>
 *   <li>{@code GitHubApiException}           → 502</li>
 * </ul>
 */
@Validated
@RestController
@RequestMapping("/repos")
public class RepositoryController {

    private static final Logger log = LoggerFactory.getLogger(RepositoryController.class);

    private final RepositoryService  repositoryService;
    private final WebhookService     webhookService;
    private final CurrentUserService currentUserService;

    public RepositoryController(RepositoryService repositoryService,
                                WebhookService webhookService,
                                CurrentUserService currentUserService) {
        this.repositoryService  = repositoryService;
        this.webhookService     = webhookService;
        this.currentUserService = currentUserService;
    }

    // -------------------------------------------------------------------------
    // GET /repos
    // -------------------------------------------------------------------------

    /**
     * Lists all GitHub repositories accessible to the authenticated user.
     *
     * <p>The backend transparently fetches all GitHub pages and returns a single
     * flat list. Each entry is marked {@code selected=true} if the user has
     * already added it for tracking.
     *
     * @return {@code 200 OK} with a list of {@link RepositoryResponse}
     */
    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> listRepositories(
            @AuthenticationPrincipal OAuth2User principal) {

        User user = currentUserService.resolve(principal);
        log.debug("GET /repos: githubId={}", user.getGithubId());
        return ResponseEntity.ok(repositoryService.listRepositories(user));
    }

    // -------------------------------------------------------------------------
    // POST /repos/select
    // -------------------------------------------------------------------------

    /**
     * Adds a repository to the user's tracked list.
     *
     * <p>Ownership is validated against a fresh GitHub API call before persisting.
     * Returns the full {@link RepositoryResponse} so the frontend can update
     * local state without a follow-up {@code GET /repos}.
     *
     * @param request body containing the GitHub numeric {@code githubRepoId}
     * @return {@code 200 OK} with the persisted {@link RepositoryResponse}
     */
    @PostMapping("/select")
    public ResponseEntity<RepositoryResponse> selectRepository(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody SelectRepositoryRequest request) {

        User user = currentUserService.resolve(principal);
        log.debug("POST /repos/select: githubId={}, githubRepoId={}",
                user.getGithubId(), request.getGithubRepoId());
        return ResponseEntity.ok(repositoryService.selectRepository(user, request.getGithubRepoId()));
    }

    // -------------------------------------------------------------------------
    // POST /repos/{repositoryId}/enable
    // -------------------------------------------------------------------------

    /**
     * Enables GitHub webhook monitoring for the specified repository.
     *
     * <p>Ownership is verified before any GitHub call. If a webhook already
     * exists for this application's URL, it is reused rather than duplicated.
     *
     * @param repositoryId the internal database ID of the repository; must be positive
     * @return {@code 200 OK} with the updated {@link RepositoryResponse}
     *         ({@code webhookEnabled=true}, {@code webhookId} populated)
     */
    @PostMapping("/{repositoryId}/enable")
    public ResponseEntity<RepositoryResponse> enableMonitoring(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable @Positive Long repositoryId) {

        User user = currentUserService.resolve(principal);
        log.debug("POST /repos/{}/enable: githubId={}", repositoryId, user.getGithubId());
        return ResponseEntity.ok(webhookService.enableMonitoring(user, repositoryId));
    }

    // -------------------------------------------------------------------------
    // POST /repos/{repositoryId}/disable
    // -------------------------------------------------------------------------

    /**
     * Disables GitHub webhook monitoring for the specified repository.
     *
     * <p>Ownership is verified before any GitHub call. If monitoring is already
     * disabled, returns the current state immediately without calling GitHub.
     * The operation is fully idempotent.
     *
     * @param repositoryId the internal database ID of the repository; must be positive
     * @return {@code 200 OK} with the updated {@link RepositoryResponse}
     *         ({@code webhookEnabled=false}, {@code webhookId=null})
     */
    @PostMapping("/{repositoryId}/disable")
    public ResponseEntity<RepositoryResponse> disableMonitoring(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable @Positive Long repositoryId) {

        User user = currentUserService.resolve(principal);
        log.debug("POST /repos/{}/disable: githubId={}", repositoryId, user.getGithubId());
        return ResponseEntity.ok(webhookService.disableMonitoring(user, repositoryId));
    }
}
