package com.prreviewer.exception;

/**
 * Exception thrown when the GitHub API rejects a request due to validation errors (e.g. HTTP 422).
 * For example, this occurs if a review comment is anchored to a line that is not in the PR diff.
 */
public class GitHubValidationException extends GitHubApiException {
    public GitHubValidationException(String message) {
        super(message);
    }

    public GitHubValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
