package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps a single file returned by the GitHub REST API "List pull requests files" endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPullRequestFileDto(
        String filename,
        String status,    // added, modified, removed, renamed, copied, changed, unchanged
        int additions,
        int deletions,
        int changes,
        String patch      // The inline diff segment for this file (may be null/missing for binary or large files)
) {}
