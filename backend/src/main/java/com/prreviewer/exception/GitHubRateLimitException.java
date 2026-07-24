package com.prreviewer.exception;

/**
 * Thrown when a GitHub API call is rejected due to rate limiting
 * (GitHub returns HTTP 403 with a rate-limit header, distinct from
 * a regular 403 permission error).
 *
 * <p>Mapped to {@code HTTP 503 Service Unavailable} by {@link GlobalExceptionHandler}
 * so the frontend and any upstream clients know to back off and retry later.
 *
 * <p>Do NOT retry immediately when this exception is thrown — GitHub rate
 * limits reset on a time window. Log the reset time and surface it in the
 * response if available.
 */
public class GitHubRateLimitException extends RuntimeException {
    public GitHubRateLimitException(String message) {
        super(message);
    }
}
