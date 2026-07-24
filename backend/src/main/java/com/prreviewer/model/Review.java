package com.prreviewer.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single AI-generated code review for a Pull Request.
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequest pullRequest;

    @Column(name = "ai_model", nullable = false)
    private String aiModel;

    /** Webhook delivery ID — used for idempotency. */
    @Column(name = "delivery_id", nullable = false, unique = true)
    private String deliveryId;

    @Column(name = "review_time_ms")
    private Long reviewTimeMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ---- Constructors ----

    public Review() {}

    private Review(Builder builder) {
        this.pullRequest  = builder.pullRequest;
        this.aiModel      = builder.aiModel;
        this.deliveryId   = builder.deliveryId;
        this.reviewTimeMs = builder.reviewTimeMs;
    }

    // ---- Getters & Setters ----

    public Long getId()                      { return id; }
    public PullRequest getPullRequest()      { return pullRequest; }
    public void setPullRequest(PullRequest v){ this.pullRequest = v; }
    public String getAiModel()               { return aiModel; }
    public void setAiModel(String v)         { this.aiModel = v; }
    public String getDeliveryId()            { return deliveryId; }
    public void setDeliveryId(String v)      { this.deliveryId = v; }
    public Long getReviewTimeMs()            { return reviewTimeMs; }
    public void setReviewTimeMs(Long v)      { this.reviewTimeMs = v; }
    public Instant getCreatedAt()            { return createdAt; }
    public List<Comment> getComments()       { return comments; }

    // ---- Builder ----

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private PullRequest pullRequest;
        private String aiModel, deliveryId;
        private Long reviewTimeMs;
        public Builder pullRequest(PullRequest v)  { this.pullRequest  = v; return this; }
        public Builder aiModel(String v)           { this.aiModel      = v; return this; }
        public Builder deliveryId(String v)        { this.deliveryId   = v; return this; }
        public Builder reviewTimeMs(Long v)        { this.reviewTimeMs = v; return this; }
        public Review build()                      { return new Review(this); }
    }
}
