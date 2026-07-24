package com.prreviewer.review;

import org.springframework.stereotype.Component;

/**
 * Pure component responsible for validating parsed ReviewComments.
 * Ensures the comment is safe to send to the GitHub API payload generation.
 * This component is purely functional and has no side effects (no logging).
 */
@Component
public class ReviewCommentValidator {

    /**
     * Validates a ReviewComment.
     * Checks if path is not blank, body is not blank, and line > 0.
     *
     * @param comment the review comment to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(ReviewComment comment) {
        if (comment.path() == null || comment.path().isBlank()) {
            return false;
        }
        if (comment.line() == null || comment.line() <= 0) {
            return false;
        }
        if (comment.body() == null || comment.body().isBlank()) {
            return false;
        }
        return true;
    }
}
