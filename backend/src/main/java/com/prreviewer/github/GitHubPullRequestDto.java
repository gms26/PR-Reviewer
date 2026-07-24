package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the core metadata returned by the GitHub REST API for a Pull Request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPullRequestDto(
        int number,
        String title,
        String body,
        String state,
        CommitRef head,
        CommitRef base,
        UserRef user
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitRef(String sha, String ref, RepoRef repo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RepoRef(String name, UserRef owner) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserRef(String login) {}
}
