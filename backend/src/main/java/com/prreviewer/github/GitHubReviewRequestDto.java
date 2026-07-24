package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Transport DTO representing the root payload for the GitHub Pull Request Review API.
 */
public record GitHubReviewRequestDto(
        @JsonProperty("commit_id") String commitId,
        String event,
        List<GitHubReviewCommentDto> comments
) {}
