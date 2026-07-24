package com.prreviewer.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /repos/select}.
 *
 * <p>The client submits the GitHub repository ID of the repo it wants to
 * monitor. Ownership validation in {@code RepositoryService} will verify
 * this ID against a fresh GitHub API call before persisting anything.
 *
 * <p><strong>Security note:</strong> The client must never be able to select
 * an arbitrary repository by guessing an ID. The {@code githubRepoId} submitted
 * here is treated as untrusted input until ownership is confirmed.
 */
public class SelectRepositoryRequest {

    @NotNull(message = "githubRepoId is required")
    private Long githubRepoId;

    // ---- Default constructor (required for Jackson deserialization) ----

    public SelectRepositoryRequest() {}

    // ---- Getter / Setter ----

    public Long getGithubRepoId()              { return githubRepoId; }
    public void setGithubRepoId(Long v)        { this.githubRepoId = v; }
}
