package com.prreviewer.github;

import com.prreviewer.model.Repository;
import com.prreviewer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates GitHub REST API calls to assemble all required data for a Pull Request.
 *
 * <p>This service acts as a facade over {@link GitHubService}. It fetches the
 * PR metadata, the paginated list of changed files, and the raw diff string,
 * then aggregates them into a single {@link PullRequestData} object.
 *
 * <p>It sits in the {@code com.prreviewer.github} package because it heavily
 * coordinates GitHub-specific API calls and DTOs. It is infrastructure, not
 * domain business logic.
 *
 * <h2>Phase 1 Scope</h2>
 * <p>This service operates synchronously. While fetching data from GitHub adds
 * latency, keeping it synchronous simplifies debugging and testing. Asynchronous
 * processing and queueing will be introduced in a later phase.
 */
@Service
public class GitHubPullRequestService {

    private static final Logger log = LoggerFactory.getLogger(GitHubPullRequestService.class);

    private final GitHubService gitHubService;

    public GitHubPullRequestService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    /**
     * Fetches all necessary Pull Request data from GitHub.
     *
     * <p>This method performs three separate GitHub API calls sequentially:
     * <ol>
     *   <li>GET pull request metadata</li>
     *   <li>GET pull request files (paginated)</li>
     *   <li>GET pull request raw diff</li>
     * </ol>
     *
     * @param user     the user to whose token will be used for authentication
     * @param repo     the repository where the PR lives
     * @param prNumber the pull request number
     * @return an aggregated {@link PullRequestData} object; remains in-memory
     */
    public PullRequestData fetchAllPullRequestData(User user, Repository repo, int prNumber) {
        log.info("Starting PR data fetch: owner={}, repo={}, prNumber={}", 
                repo.getOwner(), repo.getName(), prNumber);
        long start = System.currentTimeMillis();

        String accessToken = user.getAccessToken();
        String owner = repo.getOwner();
        String name = repo.getName();

        // 1. Fetch metadata
        GitHubPullRequestDto metadata = gitHubService.fetchPullRequest(accessToken, owner, name, prNumber);

        // 2. Fetch changed files (paginated automatically inside GitHubService)
        List<GitHubPullRequestFileDto> files = gitHubService.fetchPullRequestFiles(accessToken, owner, name, prNumber);

        // 3. Fetch raw diff
        String rawDiff = gitHubService.fetchPullRequestDiff(accessToken, owner, name, prNumber);

        // Assemble aggregate
        PullRequestData data = new PullRequestData(metadata, files, rawDiff, Instant.now());

        log.info("Completed PR data fetch: owner={}, repo={}, prNumber={}, files={}, diffSize={} bytes, duration={}ms",
                owner, name, prNumber, files.size(), rawDiff.length(), System.currentTimeMillis() - start);

        return data;
    }
}
