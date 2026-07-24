package com.prreviewer.review;

import org.springframework.stereotype.Component;

/**
 * Pure component responsible for formatting AI review findings into a readable string.
 * Isolates the presentation logic (e.g., Markdown formatting, enum prefixing)
 * from the mapping and validation logic.
 */
@Component
public class CommentFormatter {

    /**
     * Formats the body of a review comment based on its finding details.
     * Prepends severity and category if they are present.
     *
     * @param finding the AI review finding
     * @return the formatted comment string
     */
    public String format(ReviewFinding finding) {
        StringBuilder body = new StringBuilder();
        
        if (finding.severity() != null || finding.category() != null) {
            if (finding.severity() != null) {
                body.append("[").append(finding.severity().name()).append("]");
            }
            if (finding.category() != null) {
                body.append("[").append(finding.category().name()).append("]");
            }
            body.append("\n\n");
        }
        
        if (finding.comment() != null && !finding.comment().isBlank()) {
            body.append(finding.comment());
        }
        
        return body.toString().trim();
    }
}
