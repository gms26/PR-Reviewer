package com.prreviewer.service;

import com.prreviewer.dto.RepositoryResponse;
import com.prreviewer.exception.RepositoryOwnershipException;
import com.prreviewer.github.GitHubRepoDto;
import com.prreviewer.github.GitHubService;
import com.prreviewer.model.Repository;
import com.prreviewer.model.User;
import com.prreviewer.repository.RepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Business logic for repository management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li><strong>List:</strong> fetch all repositories accessible to the user from GitHub,
 *       mark which are already being monitored, and return a sorted flat list.</li>
 *   <li><strong>Select:</strong> validate ownership via a fresh GitHub API call,
 *       persist the repository if not already tracked, and return the full
 *       repository state so the frontend can update itself without a follow-up GET.</li>
 * </ul>
 *
 * <h2>Ownership validation</h2>
 * <p>Ownership validation always uses fresh data from GitHub via
 * {@link GitHubService#fetchRepositoryById(String, long)}. The repository
 * list previously sent to the frontend is <strong>never trusted</strong>.
 * This prevents clients from selecting arbitrary repositories by guessing IDs,
 * even if access was revoked after the list was fetched.
 *
 * <h2>Rate limiting</h2>
 * <p>If GitHub returns a rate-limit error, {@link GitHubService} throws
 * {@link com.prreviewer.exception.GitHubRateLimitException}, which propagates
 * up and is mapped to HTTP 503 by the global exception handler. This service
 * does not catch or retry rate-limit exceptions.
 */
@Service
public class RepositoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryService.class);

    private final GitHubService          githubService;
    private final RepositoryRepository   repositoryRepository;

    public RepositoryService(GitHubService githubService,
                             RepositoryRepository repositoryRepository) {
        this.githubService        = githubService;
        this.repositoryRepository = repositoryRepository;
    }

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    /**
     * Returns all repositories accessible to the authenticated user from GitHub,
     * with each entry marked as {@code selected} if it is already being monitored.
     *
     * <p>Sorted alphabetically by {@code fullName} (case-insensitive) for
     * consistent display order regardless of GitHub's return order.
     *
     * @param user the authenticated user (must have a valid access token)
     * @return the sorted list of repositories; never null, may be empty
     */
    public List<RepositoryResponse> listRepositories(User user) {
        log.debug("Listing repositories for user: githubId={}", user.getGithubId());

        List<GitHubRepoDto> githubRepos = githubService.fetchAllRepositories(user.getAccessToken());

        // Load all persisted repos for this user to get entity state (id, webhookEnabled)
        List<Repository> persistedRepos = repositoryRepository.findByUserId(user.getId());
        var persistedMap = persistedRepos.stream()
                .collect(java.util.stream.Collectors.toMap(Repository::getGithubRepoId, r -> r));

        List<RepositoryResponse> result = githubRepos.stream()
                .map(dto -> {
                    Repository persisted = persistedMap.get(dto.getId());
                    boolean selected = (persisted != null);
                    Long id = selected ? persisted.getId() : null;
                    boolean webhookEnabled = selected && Boolean.TRUE.equals(persisted.getWebhookEnabled());
                    return RepositoryResponse.from(dto, selected, id, webhookEnabled);
                })
                .sorted(Comparator.comparing(
                        r -> r.getFullName() != null ? r.getFullName().toLowerCase() : ""))
                .toList();

        log.debug("Returning {} repositories ({} selected) for user githubId={}",
                result.size(), persistedRepos.size(), user.getGithubId());

        return result;
    }

    /**
     * Validates ownership of the given repository and persists it as monitored.
     *
     * <p>Flow:
     * <ol>
     *   <li>Call GitHub API to confirm the repo is accessible to this user's token.</li>
     *   <li>If not found → throw {@link RepositoryOwnershipException} (→ HTTP 403).</li>
     *   <li>If already persisted for this user → return the existing record (idempotent).</li>
     *   <li>Otherwise → persist and return the new entity.</li>
     * </ol>
     *
     * <p>The returned {@link RepositoryResponse} contains the full entity state
     * (including {@code webhookEnabled}) so the frontend can update its local
     * state without a follow-up GET.
     *
     * @param user         the authenticated user
     * @param githubRepoId the GitHub repository ID submitted by the client
     * @return the persisted (or existing) repository state
     * @throws RepositoryOwnershipException if the repo is not in the user's accessible list
     * @throws com.prreviewer.exception.GitHubRateLimitException if GitHub rate-limits the check
     * @throws com.prreviewer.exception.GitHubApiException if the GitHub API call fails
     */
    @Transactional
    public RepositoryResponse selectRepository(User user, long githubRepoId) {
        log.debug("selectRepository: user githubId={}, requested githubRepoId={}",
                user.getGithubId(), githubRepoId);

        // ---- Step 1: Ownership validation via fresh GitHub data ----
        // Never trust the ID sent by the client without confirming it against GitHub.
        GitHubRepoDto verifiedRepo = githubService
                .fetchRepositoryById(user.getAccessToken(), githubRepoId)
                .orElseThrow(() -> {
                    log.warn("Ownership validation failed: githubRepoId={} not found in repos " +
                             "accessible to user githubId={}. Possible ID-guessing attempt.",
                             githubRepoId, user.getGithubId());
                    return new RepositoryOwnershipException(
                            "Repository " + githubRepoId + " is not accessible to user " + user.getGithubId());
                });

        // ---- Step 2: Idempotency — return existing record if already persisted ----
        return repositoryRepository
                .findByGithubRepoIdAndUserId(githubRepoId, user.getId())
                .map(existing -> {
                    log.debug("Repository githubRepoId={} already monitored for user githubId={}. " +
                              "Returning existing record.", githubRepoId, user.getGithubId());
                    return RepositoryResponse.from(existing);
                })
                .orElseGet(() -> persistNewRepository(user, verifiedRepo));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Persists a new {@link Repository} entity and returns its response DTO.
     * Only called when ownership has been confirmed and no existing record was found.
     */
    private RepositoryResponse persistNewRepository(User user, GitHubRepoDto dto) {
        Repository repo = Repository.builder()
                .githubRepoId(dto.getId())
                .owner(dto.getOwnerLogin())
                .name(dto.getName())
                .fullName(dto.getFullName())
                .webhookEnabled(false)
                .user(user)
                .build();

        Repository saved = repositoryRepository.save(repo);

        log.info("Repository persisted: githubRepoId={}, fullName={}, user githubId={}",
                saved.getGithubRepoId(), saved.getFullName(), user.getGithubId());

        return RepositoryResponse.from(saved);
    }
}
