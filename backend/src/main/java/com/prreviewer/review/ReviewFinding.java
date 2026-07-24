package com.prreviewer.review;

/**
 * A domain-independent representation of a single code review comment generated
 * by an AI.
 *
 * <p>This model explicitly decouples the rest of the application (e.g. GitHub
 * comment posting logic) from the specific JSON format returned by the Gemini API.
 *
 * @param file     The exact filename the finding applies to, matching the GitHub path.
 * @param line     The line number the finding applies to. May be null if the finding
 *                 is a general observation about the entire file.
 * @param severity The severity of the finding.
 * @param category The categorization of the finding.
 * @param comment  The actual review text to display to the user.
 */
public record ReviewFinding(
        String file,
        Integer line,
        Severity severity,
        Category category,
        String comment
) {}
