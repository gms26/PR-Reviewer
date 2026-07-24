package com.prreviewer.repository;

import com.prreviewer.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Review} entities.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPullRequestId(Long pullRequestId);

    /**
     * Lookup by delivery ID for idempotency enforcement.
     * GitHub may resend the same webhook delivery; this prevents duplicate processing.
     */
    Optional<Review> findByDeliveryId(String deliveryId);

    boolean existsByDeliveryId(String deliveryId);
}
