package com.prreviewer.dto;

import com.prreviewer.model.User;

import java.time.Instant;

/**
 * Safe public representation of an authenticated user.
 *
 * <p>This DTO is the only user data structure returned by the API.
 * It intentionally omits {@code accessToken} — that field must never
 * leave the server boundary.
 *
 * <p>Uses a static factory method {@link #from(User)} to enforce that
 * the mapping from entity to DTO is always done in one place.
 */
public final class UserResponse {

    private final Long    id;
    private final String  githubId;
    private final String  username;
    private final String  email;
    private final Instant createdAt;
    private final Instant lastLoginAt;

    private UserResponse(Long id, String githubId, String username,
                         String email, Instant createdAt, Instant lastLoginAt) {
        this.id          = id;
        this.githubId    = githubId;
        this.username    = username;
        this.email       = email;
        this.createdAt   = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * Creates a {@code UserResponse} from a {@link User} entity.
     * This is the single, authoritative mapping point.
     *
     * @param user the authenticated user entity (must not be null)
     * @return a safe, token-free representation of the user
     */
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getGithubId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }

    // ---- Getters ----
    // Jackson serializes via getters by default.

    public Long    getId()          { return id; }
    public String  getGithubId()    { return githubId; }
    public String  getUsername()    { return username; }
    public String  getEmail()       { return email; }
    public Instant getCreatedAt()   { return createdAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
}
