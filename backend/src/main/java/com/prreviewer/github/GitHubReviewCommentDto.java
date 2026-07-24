package com.prreviewer.github;

/**
 * Transport DTO representing a single line-level comment within a GitHub Pull Request Review payload.
 */
public record GitHubReviewCommentDto(
        String path,
        Integer line,
        String side,
        String body
) {}
