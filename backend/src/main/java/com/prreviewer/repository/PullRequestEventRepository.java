package com.prreviewer.repository;

import com.prreviewer.model.PullRequestEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link PullRequestEvent} entities.
 *
 * <p>The primary query here is the idempotency check: before inserting a new
 * event we verify that the delivery has not already been stored.  This is a
 * belt-and-suspenders guard — the {@code UNIQUE (delivery_id)} DB constraint
 * is the authoritative enforcement layer.
 */
@Repository
public interface PullRequestEventRepository extends JpaRepository<PullRequestEvent, Long> {

    /**
     * Returns {@code true} if an event row already exists for the given
     * {@code X-GitHub-Delivery} UUID.
     *
     * @param deliveryId the value of the {@code X-GitHub-Delivery} header
     */
    boolean existsByDeliveryId(String deliveryId);
}
