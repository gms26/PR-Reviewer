package com.prreviewer.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prreviewer.exception.ReviewParseException;
import com.prreviewer.review.Category;
import com.prreviewer.review.ReviewFinding;
import com.prreviewer.review.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pure component responsible for parsing the raw Gemini AI text response
 * into a structured list of {@link ReviewFinding} domain models.
 */
@Component
public class ReviewParser {

    private static final Logger log = LoggerFactory.getLogger(ReviewParser.class);
    private final ObjectMapper objectMapper;

    public ReviewParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses the AI's response into a list of findings.
     *
     * <p>Tolerates whitespace and Markdown JSON code block wrappers.
     * Throws {@link ReviewParseException} on any failure, enforcing strict
     * mapping to the enums and fields.
     *
     * @param rawResponse the raw text returned by the Gemini API
     * @return a list of parsed findings
     * @throws ReviewParseException if parsing fails
     */
    public List<ReviewFinding> parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return List.of();
        }

        String cleaned = cleanMarkdown(rawResponse);

        try {
            List<GeminiReviewFindingDto> dtos = objectMapper.readValue(
                    cleaned,
                    new TypeReference<List<GeminiReviewFindingDto>>() {}
            );
            
            if (dtos == null) {
                return List.of();
            }

            return dtos.stream().map(dto -> {
                try {
                    Severity severity = dto.severity() != null ? Severity.valueOf(dto.severity().toUpperCase()) : null;
                    Category category = dto.category() != null ? Category.valueOf(dto.category().toUpperCase()) : null;
                    return new ReviewFinding(dto.file(), dto.line(), severity, category, dto.comment());
                } catch (IllegalArgumentException e) {
                    throw new ReviewParseException("Invalid enum value in AI response: " + e.getMessage(), e);
                }
            }).toList();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response as JSON. Raw response: {}", rawResponse);
            throw new ReviewParseException("Failed to parse AI response into ReviewFinding format", e);
        }
    }

    private String cleanMarkdown(String text) {
        String trimmed = text.trim();
        
        // Strip ```json ... ``` wrapper
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        
        return trimmed.trim();
    }
}
