package com.prreviewer.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Transport DTO representing the raw JSON structure returned by Gemini.
 * Maps to the requested JSON schema in PromptBuilder.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiReviewFindingDto(
        String file,
        Integer line,
        String severity,
        String category,
        String comment
) {}
