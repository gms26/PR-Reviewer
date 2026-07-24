package com.prreviewer.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GitHub Pull Request received via webhook.
 *
 * <h2>Milestone 6 additions</h2>
 * <ul>
 *   <li>{@code headSha}       — HEAD commit SHA at the time of the last event;
 *       updated on every {@code synchronize} delivery. Required by Milestone 7
 *       to fetch the diff.</li>
 *   <li>{@code githubPrNodeId} — GraphQL node id delivered in the payload;
 *       useful for future GraphQL-based lookups.</li>
 *   <li>{@code events}         — append-only log of every
 *       {@link PullRequestEvent} received for this PR.</li>
 * </ul>
 */
@Entity
@Table(name = "pull_requests", indexes = {
    @Index(name = "idx_pr_repo_number", columnList = "repositoryId, githubPrNumber", unique = true)
})
public class PullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_pr_number", nullable = false)
    private Integer githubPrNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PullRequestStatus status = PullRequestStatus.PENDING;

    /**
     * HEAD commit SHA at the time of the most recent webhook event.
     * Set on {@code opened}; updated on every {@code synchronize} event.
     * Null until first event is processed.
     */
    @Column(name = "head_sha", length = 40)
    private String headSha;

    /**
     * GitHub GraphQL node id for this pull request (e.g. {@code "PR_kgDO..."}).
     * Delivered in the webhook payload; stored for future GraphQL use.
     * Null until first event is processed.
     */
    @Column(name = "github_pr_node_id", length = 255)
    private String githubPrNodeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /** Append-only log of all webhook events received for this PR. */
    @OneToMany(mappedBy = "pullRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PullRequestEvent> events = new ArrayList<>();

    // ---- Constructors ----

    public PullRequest() {}

    private PullRequest(Builder builder) {
        this.githubPrNumber  = builder.githubPrNumber;
        this.repository      = builder.repository;
        this.title           = builder.title;
        this.description     = builder.description;
        this.status          = builder.status;
        this.headSha         = builder.headSha;
        this.githubPrNodeId  = builder.githubPrNodeId;
    }

    // ---- Getters & Setters ----

    public Long getId()                              { return id; }
    public Integer getGithubPrNumber()               { return githubPrNumber; }
    public void setGithubPrNumber(Integer v)         { this.githubPrNumber   = v; }
    public Repository getRepository()                { return repository; }
    public void setRepository(Repository v)          { this.repository       = v; }
    public String getTitle()                         { return title; }
    public void setTitle(String v)                   { this.title            = v; }
    public String getDescription()                   { return description; }
    public void setDescription(String v)             { this.description      = v; }
    public PullRequestStatus getStatus()             { return status; }
    public void setStatus(PullRequestStatus v)       { this.status           = v; }
    public String getHeadSha()                       { return headSha; }
    public void setHeadSha(String v)                 { this.headSha          = v; }
    public String getGithubPrNodeId()                { return githubPrNodeId; }
    public void setGithubPrNodeId(String v)          { this.githubPrNodeId   = v; }
    public Instant getCreatedAt()                    { return createdAt; }
    public List<Review> getReviews()                 { return reviews; }
    public List<PullRequestEvent> getEvents()        { return events; }

    // ---- Builder ----

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private Integer           githubPrNumber;
        private Repository        repository;
        private String            title, description;
        private PullRequestStatus status = PullRequestStatus.PENDING;
        private String            headSha;
        private String            githubPrNodeId;

        public Builder githubPrNumber(Integer v)   { this.githubPrNumber  = v; return this; }
        public Builder repository(Repository v)    { this.repository      = v; return this; }
        public Builder title(String v)             { this.title           = v; return this; }
        public Builder description(String v)       { this.description     = v; return this; }
        public Builder status(PullRequestStatus v) { this.status          = v; return this; }
        public Builder headSha(String v)           { this.headSha         = v; return this; }
        public Builder githubPrNodeId(String v)    { this.githubPrNodeId  = v; return this; }
        public PullRequest build()                 { return new PullRequest(this); }
    }
}
