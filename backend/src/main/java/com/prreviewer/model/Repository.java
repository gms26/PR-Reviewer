package com.prreviewer.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GitHub repository selected for PR review monitoring.
 */
@Entity
@Table(name = "repositories", indexes = {
    @Index(name = "idx_repos_github_repo_id", columnList = "githubRepoId", unique = true)
})
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_repo_id", nullable = false, unique = true)
    private Long githubRepoId;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String name;

    /**
     * The full repository name in {@code owner/repo} format,
     * e.g. {@code "octocat/hello-world"}.
     *
     * <p>Stored explicitly to avoid rebuilding this string repeatedly in
     * later milestones (webhook registration, PR APIs, review comment URLs).
     */
    @Column(name = "full_name", nullable = false, length = 500)
    private String fullName;

    @Column(name = "webhook_enabled", nullable = false)
    private Boolean webhookEnabled = false;

    /**
     * The GitHub-assigned numeric ID of the webhook registered for this repository.
     *
     * <p>Null when no webhook has been created yet, or after it has been deleted.
     * Populated by {@code WebhookService} when monitoring is enabled, and cleared
     * to {@code null} when monitoring is disabled.
     *
     * <p>Always kept in sync with {@code webhookEnabled}:
     * <ul>
     *   <li>{@code webhookEnabled = true,  webhookId = <id>}  → actively monitoring</li>
     *   <li>{@code webhookEnabled = false, webhookId = null}  → not monitoring</li>
     * </ul>
     */
    @Column(name = "webhook_id")
    private Long webhookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PullRequest> pullRequests = new ArrayList<>();

    // ---- Constructors ----

    public Repository() {}

    private Repository(Builder builder) {
        this.githubRepoId  = builder.githubRepoId;
        this.owner         = builder.owner;
        this.name          = builder.name;
        this.fullName      = builder.fullName;
        this.webhookEnabled = builder.webhookEnabled;
        this.user          = builder.user;
    }

    // ---- Getters & Setters ----

    public Long getId()                         { return id; }
    public Long getGithubRepoId()               { return githubRepoId; }
    public void setGithubRepoId(Long v)         { this.githubRepoId = v; }
    public String getOwner()                    { return owner; }
    public void setOwner(String v)              { this.owner = v; }
    public String getName()                     { return name; }
    public void setName(String v)               { this.name = v; }
    public String getFullName()                 { return fullName; }
    public void setFullName(String v)           { this.fullName = v; }
    public Boolean getWebhookEnabled()          { return webhookEnabled; }
    public void setWebhookEnabled(Boolean v)    { this.webhookEnabled = v; }
    public Long getWebhookId()                  { return webhookId; }
    public void setWebhookId(Long v)            { this.webhookId = v; }
    public User getUser()                       { return user; }
    public void setUser(User v)                 { this.user = v; }
    public List<PullRequest> getPullRequests()  { return pullRequests; }

    // ---- Builder ----

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Long githubRepoId;
        private String owner, name, fullName;
        private Boolean webhookEnabled = false;
        private User user;
        public Builder githubRepoId(Long v)     { this.githubRepoId  = v; return this; }
        public Builder owner(String v)          { this.owner         = v; return this; }
        public Builder name(String v)           { this.name          = v; return this; }
        public Builder fullName(String v)       { this.fullName      = v; return this; }
        public Builder webhookEnabled(Boolean v){ this.webhookEnabled = v; return this; }
        public Builder user(User v)             { this.user          = v; return this; }
        public Repository build()               { return new Repository(this); }
    }
}
