package com.prreviewer.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;

/**
 * Pure mapping component that converts raw AI ReviewFindings into valid,
 * domain-level ReviewComments, leveraging the CommentFormatter and Validator.
 */
@Component
public class ReviewCommentMapper {

    private static final Logger log = LoggerFactory.getLogger(ReviewCommentMapper.class);

    private final CommentFormatter commentFormatter;
    private final ReviewCommentValidator validator;

    public ReviewCommentMapper(CommentFormatter commentFormatter, ReviewCommentValidator validator) {
        this.commentFormatter = commentFormatter;
        this.validator = validator;
    }

    /**
     * Maps a list of ReviewFindings into a validated list of ReviewComments.
     * Skips findings that do not pass validation.
     *
     * @param findings the raw findings from the AI
     * @return a list of validated review comments
     */
    public List<ReviewComment> mapToComments(List<ReviewFinding> findings) {
        if (findings == null || findings.isEmpty()) {
            return List.of();
        }

        return findings.stream()
                .filter(Objects::nonNull)
                .map(this::toReviewComment)
                .filter(comment -> {
                    boolean valid = validator.isValid(comment);
                    if (!valid) {
                        log.warn("Skipping invalid or incomplete comment for file '{}'", comment.path());
                    }
                    return valid;
                })
                .toList();
    }

    private ReviewComment toReviewComment(ReviewFinding finding) {
        String formattedBody = commentFormatter.format(finding);
        return new ReviewComment(finding.file(), finding.line(), formattedBody);
    }
}
