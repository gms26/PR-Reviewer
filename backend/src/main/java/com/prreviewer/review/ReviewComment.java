package com.prreviewer.review;

/**
 * Pure domain model representing a single comment targeted at a specific file and line.
 * This class is platform-agnostic and isolates the AI review logic from GitHub's specific API requirements.
 */
public record ReviewComment(
        String path,
        Integer line,
        String body
) {}
