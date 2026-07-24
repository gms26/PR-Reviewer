package com.prreviewer.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

/**
 * Records a single GitHub {@code pull_request} webhook event.
 *
 * <p>A single {@link PullRequest} accumulates many events over its lifetime:
 * {@code opened}, {@code synchronize}, {@code reopened}, {@code closed}, etc.
 * Storing each event as its own row gives a complete, immutable audit trail
 * without mutating the parent {@code PullRequest} record on every delivery.
 *
 * <h2>Idempotency</h2>
 * <p>{@code delivery_id} carries a {@code UNIQUE} constraint that mirrors the
 * primary key of {@link WebhookDelivery}.  A duplicate webhook delivery from
 * GitHub will fail with a {@code DataIntegrityViolationException} at the DB
 * level, which {@link com.prreviewer.service.PullRequestService} catches and
 * silently swallows.
 *
 * <h2>Phase 1 scope</h2>
 * <p>Only {@code opened} and {@code synchronize} actions ever reach this table
 * in Phase 1.  The {@code event} column is stored anyway to future-proof the
 * table if other GitHub event types are added later.
 */
@Entity
@Table(
    name = "pull_request_events",
    indexes = {
        @Index(name = "uq_pre_delivery_id",  columnList = "deliveryId",    unique = true),
        @Index(name = "idx_pre_pull_request", columnList = "pullRequestId")
    }
)
public class PullRequestEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent pull request.  Invariant: never null — this event only exists
     * because a persisted {@link PullRequest} was matched or created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequest pullRequest;

    /**
     * The {@code X-GitHub-Delivery} UUID for the webhook delivery that produced
     * this event.  Acts as the natural idempotency key.
     *
     * <p>Also a foreign key into {@link WebhookDelivery} (the raw payload store).
     */
    @Column(name = "delivery_id", length = 36, nullable = false, unique = true)
    private String deliveryId;

    /**
     * The {@code X-GitHub-Event} header value.  Always {@code "pull_request"}
     * in Phase 1, stored for forward compatibility.
     */
    @Column(name = "event", length = 64, nullable = false)
    private String event;

    /**
     * The {@code action} field from the webhook payload
     * (e.g. {@code "opened"}, {@code "synchronize"}).
     */
    @Column(name = "action", length = 64, nullable = false)
    private String action;

    /** GitHub username of the user who triggered the event. */
    @Column(name = "sender_login", length = 255, nullable = false)
    private String senderLogin;

    /**
     * The GitHub App installation ID, present only when the webhook was
     * delivered via a GitHub App (as opposed to a personal OAuth token).
     * Nullable.
     */
    @Column(name = "installation_id")
    private Long installationId;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    // ---- Constructors ----

    protected PullRequestEvent() {
        // JPA requires a no-arg constructor
    }

    private PullRequestEvent(Builder builder) {
        this.pullRequest    = builder.pullRequest;
        this.deliveryId     = builder.deliveryId;
        this.event          = builder.event;
        this.action         = builder.action;
        this.senderLogin    = builder.senderLogin;
        this.installationId = builder.installationId;
    }

    // ---- Getters ----

    public Long          getId()             { return id; }
    public PullRequest   getPullRequest()    { return pullRequest; }
    public String        getDeliveryId()     { return deliveryId; }
    public String        getEvent()          { return event; }
    public String        getAction()         { return action; }
    public String        getSenderLogin()    { return senderLogin; }
    public Long          getInstallationId() { return installationId; }
    public Instant       getReceivedAt()     { return receivedAt; }

    // ---- Equality (natural key = deliveryId) ----

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PullRequestEvent that)) return false;
        return Objects.equals(deliveryId, that.deliveryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId);
    }

    // ---- Builder ----

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private PullRequest pullRequest;
        private String      deliveryId;
        private String      event;
        private String      action;
        private String      senderLogin;
        private Long        installationId;

        public Builder pullRequest(PullRequest v)    { this.pullRequest    = v; return this; }
        public Builder deliveryId(String v)          { this.deliveryId     = v; return this; }
        public Builder event(String v)               { this.event          = v; return this; }
        public Builder action(String v)              { this.action         = v; return this; }
        public Builder senderLogin(String v)         { this.senderLogin    = v; return this; }
        public Builder installationId(Long v)        { this.installationId = v; return this; }
        public PullRequestEvent build()              { return new PullRequestEvent(this); }
    }
}
