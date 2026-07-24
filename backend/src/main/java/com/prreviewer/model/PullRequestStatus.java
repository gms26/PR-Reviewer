package com.prreviewer.model;

/**
 * Lifecycle states for a Pull Request review.
 */
public enum PullRequestStatus {
    /** Webhook received but review has not started yet. */
    PENDING,

    /** Review is in progress (context built, AI called). */
    IN_PROGRESS,

    /** Review completed and comments posted successfully. */
    COMPLETED,

    /** Review failed (AI parse error, API failure, etc.). */
    FAILED
}
