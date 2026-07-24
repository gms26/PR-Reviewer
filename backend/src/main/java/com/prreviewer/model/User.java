package com.prreviewer.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GitHub user who has authenticated via OAuth.
 *
 * <p>Identity contract:
 * <ul>
 *   <li>{@code id} — internal surrogate key, never exposed externally.</li>
 *   <li>{@code githubId} — permanent GitHub user ID (numeric string). This is
 *       the stable identity anchor. Username and email can change on GitHub;
 *       this never does.</li>
 *   <li>{@code createdAt} — set once on INSERT, {@code updatable = false} enforced
 *       at the JPA column level. Never touched again.</li>
 *   <li>{@code lastLoginAt} — updated on every successful OAuth login by
 *       {@link com.prreviewer.auth.UserService}.</li>
 * </ul>
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_github_id", columnList = "githubId", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", nullable = false, unique = true)
    private String githubId;

    @Column(nullable = false)
    private String username;

    @Column
    private String email;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    /**
     * Set once at INSERT time. {@code updatable = false} is enforced at the
     * column DDL level — Hibernate will never include this column in an UPDATE
     * statement, regardless of what the application code does.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Updated on every successful OAuth login.
     *
     * <p>NOT NULL — the database enforces this constraint (see V2 migration).
     * {@link com.prreviewer.auth.UserService} always sets this value before
     * persisting a user, so it should never be null in a correctly running system.
     *
     * <p>For new users: set to {@code Instant.now()} at the moment of first login.
     * For existing users: updated to {@code Instant.now()} on every subsequent login.
     */
    @Column(name = "last_login_at", nullable = false)
    private Instant lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Repository> repositories = new ArrayList<>();

    // ---- Constructors ----

    public User() {}

    private User(Builder builder) {
        this.githubId    = builder.githubId;
        this.username    = builder.username;
        this.email       = builder.email;
        this.accessToken = builder.accessToken;
    }

    // ---- Getters & Setters ----

    public Long    getId()                       { return id; }

    public String  getGithubId()                 { return githubId; }
    public void    setGithubId(String v)         { this.githubId = v; }

    public String  getUsername()                 { return username; }
    public void    setUsername(String v)         { this.username = v; }

    public String  getEmail()                    { return email; }
    public void    setEmail(String v)            { this.email = v; }

    public String  getAccessToken()              { return accessToken; }
    public void    setAccessToken(String v)      { this.accessToken = v; }

    public Instant getCreatedAt()                { return createdAt; }

    public Instant getLastLoginAt()              { return lastLoginAt; }
    public void    setLastLoginAt(Instant v)     { this.lastLoginAt = v; }

    public List<Repository> getRepositories()    { return repositories; }

    // ---- Builder ----

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String githubId, username, email, accessToken;
        public Builder githubId(String v)    { this.githubId    = v; return this; }
        public Builder username(String v)    { this.username    = v; return this; }
        public Builder email(String v)       { this.email       = v; return this; }
        public Builder accessToken(String v) { this.accessToken = v; return this; }
        public User build()                  { return new User(this); }
    }
}
