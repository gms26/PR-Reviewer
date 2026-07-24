package com.prreviewer.github;

import com.prreviewer.exception.GitHubApiException;
import com.prreviewer.exception.GitHubRateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Client for the GitHub REST API v3.
 *
 * <p>This is the only class in the application that communicates directly
 * with GitHub. All other components that need GitHub data must go through
 * this service.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Fetch and paginate user repositories</li>
 *   <li>Fetch a specific repository by ID (for ownership validation)</li>
 *   <li>List, create, and delete webhooks for a repository</li>
 *   <li>Find an existing webhook by exact URL match (for duplicate detection)</li>
 *   <li>Fetch Pull Request metadata, changed files (paginated), and raw diffs</li>
 *   <li>Map GitHub HTTP errors to domain exceptions</li>
 * </ul>
 *
 * <h2>What this service does NOT do</h2>
 * <p>Business logic — deciding whether to enable monitoring, whether a webhook
 * is a duplicate, ownership validation — belongs in the service layer
 * ({@code RepositoryService}, {@code WebhookService}), not here.
 *
 * <h2>Error handling</h2>
 * <table border="1">
 *   <tr><th>GitHub HTTP status</th><th>Exception thrown</th></tr>
 *   <tr><td>401 Unauthorized</td><td>{@link GitHubApiException} (token invalid/expired)</td></tr>
 *   <tr><td>403 + rate limit header</td><td>{@link GitHubRateLimitException} → HTTP 503</td></tr>
 *   <tr><td>403 permission denied</td><td>{@link GitHubApiException} → HTTP 502</td></tr>
 *   <tr><td>404 on webhook delete</td><td>Handled inline — treated as already deleted (idempotent)</td></tr>
 *   <tr><td>Other 4xx</td><td>{@link GitHubApiException}</td></tr>
 *   <tr><td>5xx / network error</td><td>{@link GitHubApiException}</td></tr>
 * </table>
 *
 * <h2>Timeout policy</h2>
 * <p>Connect and read timeouts are configured in {@link com.prreviewer.config.RestClientConfig}.
 * GitHub API requests will never wait indefinitely.
 *
 * <h2>Logging</h2>
 * <p>Logs owner, repo, webhook ID, and operation duration at INFO/DEBUG level.
 * OAuth tokens and webhook secrets are <strong>never</strong> logged.
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    /** Maximum results per page when paginating GitHub API results. */
    private static final int PAGE_SIZE = 100;

    /** GitHub event type used to subscribe to Pull Request lifecycle events. */
    private static final String EVENT_PULL_REQUEST = "pull_request";

    /** The {@code name} field value required by GitHub for all HTTP webhooks. */
    private static final String WEBHOOK_TYPE_WEB = "web";

    private final RestClient githubRestClient;

    public GitHubService(@Qualifier("githubRestClient") RestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
    }

    // =========================================================================
    // Repository methods (Milestone 3)
    // =========================================================================

    /**
     * Fetches all repositories accessible to the user identified by the given
     * access token, transparently paginating all GitHub API pages.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @return a flat list of all accessible repositories; never null, may be empty
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public List<GitHubRepoDto> fetchAllRepositories(String accessToken) {
        log.debug("Fetching all repositories from GitHub");
        List<GitHubRepoDto> all = new ArrayList<>();
        int page = 1;

        while (true) {
            List<GitHubRepoDto> pageResult = fetchRepositoryPage(accessToken, page);
            if (pageResult.isEmpty()) break;
            all.addAll(pageResult);
            if (pageResult.size() < PAGE_SIZE) break; // last page
            page++;
        }

        log.debug("Fetched {} repositories total from GitHub", all.size());
        return all;
    }

    /**
     * Finds a specific repository by its GitHub numeric ID by scanning the
     * user's accessible repositories.
     *
     * <p>Used for ownership validation in {@code RepositoryService.selectRepository()}.
     * Always fetches fresh data from GitHub — the previously returned list is
     * never trusted for this check.
     *
     * <p>Implementation: {@link #fetchAllRepositories} → stream → findFirst.
     * {@code GitHubService} does not know how the caller searches; it exposes
     * fetch and the caller decides the lookup strategy.
     *
     * @param accessToken  the user's GitHub OAuth access token
     * @param githubRepoId the GitHub numeric repository ID to look up
     * @return the matching repository, or empty if not found or not accessible
     */
    public Optional<GitHubRepoDto> fetchRepositoryById(String accessToken, long githubRepoId) {
        return fetchAllRepositories(accessToken).stream()
                .filter(r -> githubRepoId == r.getId())
                .findFirst();
    }

    // =========================================================================
    // Webhook methods (Milestone 4)
    // =========================================================================

    /**
     * Returns all webhooks configured for the given repository.
     *
     * <p>GitHub does not paginate the webhooks list for typical repositories
     * (the limit is very high). This method fetches the single response without
     * pagination. If a repository ever has enough webhooks to paginate, this
     * should be revisited.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login (e.g. {@code "octocat"})
     * @param repo        the repository name (e.g. {@code "hello-world"})
     * @return the list of configured webhooks; never null, may be empty
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public List<GitHubWebhookDto> getWebhooks(String accessToken, String owner, String repo) {
        log.debug("Fetching webhooks for {}/{}", owner, repo);
        long start = System.currentTimeMillis();

        try {
            List<GitHubWebhookDto> webhooks = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/hooks", owner, repo)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubWebhookDto>>() {});

            List<GitHubWebhookDto> result = webhooks != null ? webhooks : List.of();
            log.debug("Found {} webhook(s) for {}/{} in {}ms",
                    result.size(), owner, repo, System.currentTimeMillis() - start);
            return result;

        } catch (HttpClientErrorException ex) {
            throw mapHttpError(ex, "getWebhooks", owner, repo, null);
        } catch (RestClientException ex) {
            throw new GitHubApiException(
                    "Network error fetching webhooks for " + owner + "/" + repo, ex);
        }
    }

    /**
     * Finds the webhook belonging to this application for the given repository,
     * using exact URL matching.
     *
     * <p>Matching rules (all must pass):
     * <ol>
     *   <li>The webhook's {@code config.url} must exactly equal {@code expectedUrl}.</li>
     *   <li>The webhook must be {@code active = true}.</li>
     *   <li>The webhook's {@code events} list must contain {@code "pull_request"}.</li>
     * </ol>
     *
     * <p>This allows development, staging, and production environments to coexist
     * safely — each environment has a distinct base URL and will only match its
     * own webhooks.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login
     * @param repo        the repository name
     * @param expectedUrl the exact webhook URL this application expects
     *                    (e.g. {@code "https://api.myapp.com/webhook/github"})
     * @return the matching webhook, or empty if none found
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public Optional<GitHubWebhookDto> findWebhookByUrl(String accessToken, String owner,
                                                        String repo, String expectedUrl) {
        List<GitHubWebhookDto> webhooks = getWebhooks(accessToken, owner, repo);
        Optional<GitHubWebhookDto> found = findExistingWebhook(webhooks, expectedUrl);

        if (found.isPresent()) {
            log.debug("Found existing webhook id={} for {}/{} matching url={}",
                    found.get().id(), owner, repo, expectedUrl);
        } else {
            log.debug("No existing webhook found for {}/{} matching url={}", owner, repo, expectedUrl);
        }

        return found;
    }

    /**
     * Creates a new webhook for the given repository.
     *
     * <p>The webhook is always created with:
     * <ul>
     *   <li>{@code name = "web"} — required by GitHub for all HTTP webhooks</li>
     *   <li>{@code active = true} — begins delivering immediately</li>
     *   <li>{@code events = ["pull_request"]} — only PR events</li>
     *   <li>{@code config.content_type = "json"}</li>
     *   <li>{@code config.insecure_ssl = "0"} — SSL verification always enabled</li>
     * </ul>
     *
     * <p>Callers cannot customize these fields. The webhook configuration is
     * fixed for this application.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login
     * @param repo        the repository name
     * @param webhookUrl  the URL GitHub will POST payloads to
     * @param secret      the HMAC-SHA256 signing secret for payload verification;
     *                    never logged
     * @return the created webhook (contains the GitHub-assigned {@code id})
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public GitHubWebhookDto createWebhook(String accessToken, String owner, String repo,
                                           String webhookUrl, String secret) {
        log.info("Creating webhook for {}/{}: url={}", owner, repo, webhookUrl);
        long start = System.currentTimeMillis();

        WebhookConfigDto config = new WebhookConfigDto(webhookUrl, "json", "0", secret);
        CreateWebhookRequest request = new CreateWebhookRequest(
                WEBHOOK_TYPE_WEB, config, List.of(EVENT_PULL_REQUEST), true);

        try {
            GitHubWebhookDto created = githubRestClient.post()
                    .uri("/repos/{owner}/{repo}/hooks", owner, repo)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .body(request)
                    .retrieve()
                    .body(GitHubWebhookDto.class);

            if (created == null) {
                throw new GitHubApiException(
                        "GitHub returned empty response when creating webhook for "
                        + owner + "/" + repo);
            }

            log.info("Webhook created: owner={}, repo={}, webhookId={}, duration={}ms",
                    owner, repo, created.id(), System.currentTimeMillis() - start);
            return created;

        } catch (HttpClientErrorException ex) {
            throw mapHttpError(ex, "createWebhook", owner, repo, null);
        } catch (RestClientException ex) {
            throw new GitHubApiException(
                    "Network error creating webhook for " + owner + "/" + repo, ex);
        }
    }

    /**
     * Deletes a webhook from the given repository.
     *
     * <p>This operation is idempotent: if GitHub returns 404 (webhook was already
     * deleted or never existed), the error is logged at INFO level and the method
     * returns normally. The caller should still update the database regardless.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login
     * @param repo        the repository name
     * @param webhookId   the GitHub-assigned webhook ID to delete
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other non-404 GitHub API error
     */
    public void deleteWebhook(String accessToken, String owner, String repo, long webhookId) {
        log.info("Deleting webhook: owner={}, repo={}, webhookId={}", owner, repo, webhookId);
        long start = System.currentTimeMillis();

        try {
            githubRestClient.delete()
                    .uri("/repos/{owner}/{repo}/hooks/{hookId}", owner, repo, webhookId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Webhook deleted: owner={}, repo={}, webhookId={}, duration={}ms",
                    owner, repo, webhookId, System.currentTimeMillis() - start);

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Treat 404 as idempotent — webhook was already deleted or never existed.
                // The caller (WebhookService) must still update the database.
                log.info("Webhook {} for {}/{} not found on GitHub — treating as already deleted",
                        webhookId, owner, repo);
                return;
            }
            throw mapHttpError(ex, "deleteWebhook", owner, repo, webhookId);
        } catch (RestClientException ex) {
            throw new GitHubApiException(
                    "Network error deleting webhook " + webhookId
                    + " for " + owner + "/" + repo, ex);
        }
    }

    // =========================================================================
    // Pull Request methods (Milestone 7)
    // =========================================================================

    /**
     * Fetches metadata for a specific Pull Request.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login
     * @param repo        the repository name
     * @param prNumber    the PR number
     * @return the pull request metadata
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public GitHubPullRequestDto fetchPullRequest(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching PR metadata for {}/{} #{}", owner, repo, prNumber);
        long start = System.currentTimeMillis();

        try {
            GitHubPullRequestDto pr = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{pullNumber}", owner, repo, prNumber)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(GitHubPullRequestDto.class);

            if (pr == null) {
                throw new GitHubApiException("GitHub returned empty response for PR " + prNumber);
            }

            log.debug("Fetched PR metadata for {}/{} #{} in {}ms",
                    owner, repo, prNumber, System.currentTimeMillis() - start);
            return pr;

        } catch (HttpClientErrorException ex) {
            throw mapHttpError(ex, "fetchPullRequest", owner, repo, null);
        } catch (RestClientException ex) {
            throw new GitHubApiException(
                    "Network error fetching PR metadata for " + owner + "/" + repo + " #" + prNumber, ex);
        }
    }

    /**
     * Fetches all changed files for a specific Pull Request, transparently paginating
     * through all pages if the PR contains many files.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login
     * @param repo        the repository name
     * @param prNumber    the PR number
     * @return a flat list of all changed files; never null, may be empty
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public List<GitHubPullRequestFileDto> fetchPullRequestFiles(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching PR files for {}/{} #{}", owner, repo, prNumber);
        long start = System.currentTimeMillis();
        List<GitHubPullRequestFileDto> allFiles = new ArrayList<>();
        int page = 1;

        while (true) {
            List<GitHubPullRequestFileDto> pageResult = fetchPullRequestFilesPage(accessToken, owner, repo, prNumber, page);
            if (pageResult.isEmpty()) break;
            allFiles.addAll(pageResult);
            if (pageResult.size() < PAGE_SIZE) break; // last page
            page++;
        }

        log.debug("Fetched {} PR file(s) for {}/{} #{} in {}ms",
                allFiles.size(), owner, repo, prNumber, System.currentTimeMillis() - start);
        return allFiles;
    }

    private List<GitHubPullRequestFileDto> fetchPullRequestFilesPage(String accessToken, String owner, String repo, int prNumber, int page) {
        try {
            List<GitHubPullRequestFileDto> result = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{pullNumber}/files?per_page={size}&page={page}",
                            owner, repo, prNumber, PAGE_SIZE, page)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubPullRequestFileDto>>() {});
            return result != null ? result : List.of();
        } catch (HttpClientErrorException ex) {
            throw mapHttpError(ex, "fetchPullRequestFilesPage", owner, repo, null);
        } catch (RestClientException ex) {
            throw new GitHubApiException(
                    "Network error fetching PR files for " + owner + "/" + repo + " #" + prNumber, ex);
        }
    }

    /**
     * Fetches the raw diff of a specific Pull Request as a plain text string.
     *
     * <p>Uses the {@code Accept: application/vnd.github.v3.diff} header to
     * instruct GitHub to return a diff instead of JSON.
     *
     * @param accessToken the user's GitHub OAuth access token
     * @param owner       the repository owner login
     * @param repo        the repository name
     * @param prNumber    the PR number
     * @return the raw diff string
     * @throws GitHubRateLimitException if GitHub's rate limit is hit
     * @throws GitHubApiException       for any other GitHub API error
     */
    public String fetchPullRequestDiff(String accessToken, String owner, String repo, int prNumber) {
        log.debug("Fetching PR raw diff for {}/{} #{}", owner, repo, prNumber);
        long start = System.currentTimeMillis();

        try {
            String diff = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{pullNumber}", owner, repo, prNumber)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github.v3.diff")
                    .retrieve()
                    .body(String.class);

            if (diff == null) {
                // An empty diff could technically be an empty string, but a null body means something went wrong.
                throw new GitHubApiException("GitHub returned null body when fetching diff for PR " + prNumber);
            }

            log.debug("Fetched PR raw diff for {}/{} #{} ({} bytes) in {}ms",
                    owner, repo, prNumber, diff.length(), System.currentTimeMillis() - start);
            return diff;

        } catch (HttpClientErrorException ex) {
            throw mapHttpError(ex, "fetchPullRequestDiff", owner, repo, null);
        } catch (RestClientException ex) {
            throw new GitHubApiException(
                    "Network error fetching PR diff for " + owner + "/" + repo + " #" + prNumber, ex);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Paginates a single page of repository results.
     * Not paginated for webhooks — see {@link #getWebhooks}.
     */
    private List<GitHubRepoDto> fetchRepositoryPage(String accessToken, int page) {
        try {
            List<GitHubRepoDto> result = githubRestClient.get()
                    .uri("/user/repos?per_page={size}&page={page}&sort=updated",
                            PAGE_SIZE, page)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepoDto>>() {});
            return result != null ? result : List.of();
        } catch (HttpClientErrorException ex) {
            throw mapHttpError(ex, "fetchRepositories", null, null, null);
        } catch (RestClientException ex) {
            throw new GitHubApiException("Network error fetching repositories from GitHub", ex);
        }
    }

    /**
     * Finds a webhook in the given list whose URL exactly matches {@code expectedUrl},
     * is active, and is subscribed to the {@code pull_request} event.
     *
     * <p>All three conditions must pass. This avoids reusing:
     * <ul>
     *   <li>Webhooks from another environment (wrong URL)</li>
     *   <li>Disabled webhooks ({@code active = false})</li>
     *   <li>Webhooks for unrelated events</li>
     * </ul>
     *
     * @param webhooks    the list of webhooks from GitHub
     * @param expectedUrl the exact URL to match against {@code config.url()}
     * @return the matching webhook, or empty if none match
     */
    private Optional<GitHubWebhookDto> findExistingWebhook(List<GitHubWebhookDto> webhooks,
                                                             String expectedUrl) {
        return webhooks.stream()
                .filter(hook -> hook.config() != null)
                .filter(hook -> expectedUrl.equals(hook.config().url()))
                .filter(GitHubWebhookDto::active)
                .filter(hook -> hook.events() != null && hook.events().contains(EVENT_PULL_REQUEST))
                .findFirst();
    }

    /**
     * Maps a GitHub HTTP client error to a domain exception.
     *
     * <p>Returns the exception rather than throwing it directly, so callers
     * can write {@code throw mapHttpError(...)} — which the compiler treats as
     * a definite exit point, eliminating the need for unreachable return statements.
     *
     * @param ex        the client error from Spring's RestClient
     * @param operation a short description of the operation for log messages
     * @param owner     the repository owner, or null if not applicable
     * @param repo      the repository name, or null if not applicable
     * @param webhookId the webhook ID, or null if not applicable
     * @return the appropriate domain exception
     */
    private RuntimeException mapHttpError(HttpClientErrorException ex,
                                           String operation,
                                           String owner, String repo, Long webhookId) {
        String context = buildContext(operation, owner, repo, webhookId);

        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return new GitHubApiException(
                    "GitHub returned 401 Unauthorized during " + context
                    + " — access token may be invalid or expired", ex);
        }

        if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            String remaining = ex.getResponseHeaders() != null
                    ? ex.getResponseHeaders().getFirst("X-RateLimit-Remaining") : null;
            String reset = ex.getResponseHeaders() != null
                    ? ex.getResponseHeaders().getFirst("X-RateLimit-Reset") : null;

            if ("0".equals(remaining)) {
                log.warn("GitHub rate limit exceeded during {}. X-RateLimit-Reset={}",
                        context, reset);
                return new GitHubRateLimitException(
                        "GitHub API rate limit exceeded. Resets at epoch second: " + reset);
            }

            return new GitHubApiException(
                    "GitHub returned 403 Forbidden during " + context
                    + " — check OAuth scopes or repository access", ex);
        }

        return new GitHubApiException(
                "GitHub returned " + ex.getStatusCode() + " during " + context, ex);
    }

    /** Builds a human-readable context string for error messages and logs. */
    private String buildContext(String operation, String owner, String repo, Long webhookId) {
        StringBuilder sb = new StringBuilder(operation);
        if (owner != null && repo != null) sb.append(" [").append(owner).append("/").append(repo).append("]");
        if (webhookId != null) sb.append(" webhookId=").append(webhookId);
        return sb.toString();
    }
}
