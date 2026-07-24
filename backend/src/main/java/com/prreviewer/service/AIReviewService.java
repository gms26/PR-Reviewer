package com.prreviewer.service;

import com.prreviewer.gemini.GeminiService;
import com.prreviewer.gemini.PromptBuilder;
import com.prreviewer.gemini.ReviewParser;
import com.prreviewer.review.ReviewContext;
import com.prreviewer.review.ReviewFinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrator service for the AI Code Review pipeline.
 *
 * <p>Coordinates the pure components and the Gemini infrastructure service to
 * transform a deterministic {@link ReviewContext} into a list of AI-generated
 * {@link ReviewFinding} objects.
 */
@Service
public class AIReviewService {

    private static final Logger log = LoggerFactory.getLogger(AIReviewService.class);

    private final PromptBuilder promptBuilder;
    private final GeminiService geminiService;
    private final ReviewParser reviewParser;

    public AIReviewService(PromptBuilder promptBuilder,
                           GeminiService geminiService,
                           ReviewParser reviewParser) {
        this.promptBuilder = promptBuilder;
        this.geminiService = geminiService;
        this.reviewParser = reviewParser;
    }

    /**
     * Executes the complete AI review pipeline.
     *
     * @param context the deterministic review context
     * @return a list of structured domain models representing the AI's findings
     */
    public List<ReviewFinding> performReview(ReviewContext context) {
        log.info("AI Review STARTED | Repository: {}/{} | PR: {}", 
                context.repositoryOwner(), context.repositoryName(), context.pullRequestNumber());
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        int findingCount = 0;

        try {
            // 1. Build the prompt
            String prompt = promptBuilder.buildPrompt(context);
            log.info("AI Review PROMPT_BUILT | PR: {} | Prompt size: {} chars", context.pullRequestNumber(), prompt.length());

            // 2. Call the AI model
            String rawResponse = geminiService.generateReview(prompt);

            // 3. Parse the structured output
            List<ReviewFinding> findings = reviewParser.parse(rawResponse);
            findingCount = findings.size();
            success = true;

            // Findings are kept purely in-memory for Milestone 9.
            return findings;
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String status = success ? "SUCCESS" : "FAILED";
            log.info("AI Review COMPLETED | PR: {} | Status: {} | Duration: {}ms | Findings: {} | Model: Gemini-2.0-Flash", 
                     context.pullRequestNumber(), status, duration, findingCount);
        }
    }
}
