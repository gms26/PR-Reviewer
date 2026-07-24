package com.prreviewer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a processed GitHub webhook delivery.
 *
 * <p>Stored to ensure idempotency. If GitHub retries a delivery
 * (which they do on timeouts), we use the {@code deliveryId} from the
 * {@code X-GitHub-Delivery} header to silently ignore the duplicate.
 *
 * <p>The full JSON payload is stored as JSONB for auditing, debugging,
 * and future offline test case generation.
 */
@Entity
@Table(name = "webhook_delivery")
public class WebhookDelivery {

    @Id
    @Column(name = "delivery_id", length = 36, nullable = false)
    private String deliveryId;

    @Column(name = "event", length = 64, nullable = false)
    private String event;

    @Column(name = "action", length = 64, nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    protected WebhookDelivery() {
        // JPA requires a no-arg constructor
    }

    public WebhookDelivery(String deliveryId, String event, String action, String payload) {
        this.deliveryId = deliveryId;
        this.event = event;
        this.action = action;
        this.payload = payload;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getEvent() {
        return event;
    }

    public String getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebhookDelivery that)) return false;
        return Objects.equals(deliveryId, that.deliveryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId);
    }
}
