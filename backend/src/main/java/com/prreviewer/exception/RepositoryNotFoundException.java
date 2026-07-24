package com.prreviewer.exception;

/**
 * Thrown when a {@link com.prreviewer.model.Repository} entity cannot be found
 * in the database by its internal ID.
 *
 * <p>This is a domain exception, not a JPA exception. Using a domain-specific
 * class keeps {@link com.prreviewer.webhook.WebhookService} (and any other
 * service layer class) independent of JPA semantics — no leaking of
 * {@code jakarta.persistence.EntityNotFoundException} into business logic.
 *
 * <p>Mapped to HTTP 404 Not Found by
 * {@link GlobalExceptionHandler#handleRepositoryNotFound(RepositoryNotFoundException)}.
 */
public class RepositoryNotFoundException extends RuntimeException {

    private final Long repositoryId;

    public RepositoryNotFoundException(Long repositoryId) {
        super("Repository not found: " + repositoryId);
        this.repositoryId = repositoryId;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }
}
