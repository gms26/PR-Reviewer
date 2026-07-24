package com.prreviewer.exception;

/**
 * Thrown when ownership validation fails — the submitted {@code githubRepoId}
 * does not appear in the list of repositories accessible to the authenticated user.
 *
 * <p>Mapped to {@code HTTP 403 Forbidden} by {@link GlobalExceptionHandler}.
 *
 * <p>Security contract: this exception must never reveal <em>why</em> the repo
 * was not found (i.e. whether it exists but is inaccessible, or does not exist
 * at all). The client-facing message is deliberately vague.
 */
public class RepositoryOwnershipException extends RuntimeException {
    public RepositoryOwnershipException(String message) {
        super(message);
    }
}
