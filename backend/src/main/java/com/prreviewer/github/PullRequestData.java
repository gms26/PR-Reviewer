package com.prreviewer.github;

import java.time.Instant;
import java.util.List;

/**
 * An aggregate container holding all GitHub data fetched for a Pull Request.
 * This object remains entirely in-memory and will be passed directly to the
 * Context Builder in later milestones.
 *
 * @param metadata  The core PR metadata (title, body, SHAs)
 * @param files     The list of all changed files (paginated and aggregated)
 * @param rawDiff   The complete raw text diff of the PR
 * @param fetchedAt When this data was retrieved from GitHub
 */
public record PullRequestData(
        GitHubPullRequestDto metadata,
        List<GitHubPullRequestFileDto> files,
        String rawDiff,
        Instant fetchedAt
) {}
