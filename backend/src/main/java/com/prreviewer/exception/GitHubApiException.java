package com.prreviewer.exception;

/**
 * Thrown when a GitHub API call fails with a non-transient error.
 * Transient failures are handled by Spring Retry before this is thrown.
 */
public class GitHubApiException extends RuntimeException {
    public GitHubApiException(String message) {
        super(message);
    }

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
