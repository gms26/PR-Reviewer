package com.prreviewer.gemini;

import com.prreviewer.review.ChangedFileContext;
import com.prreviewer.review.ReviewContext;
import org.springframework.stereotype.Component;

/**
 * Pure component for constructing the prompt string for the Gemini AI.
 */
@Component
public class PromptBuilder {

    private static final String PROMPT_VERSION = "v1";

    /**
     * Builds the complete prompt string using the given review context.
     * Includes instructions for structured JSON output and only includes
     * changed files.
     *
     * @param context the deterministic review context
     * @return the formatted prompt string
     */
    public String buildPrompt(ReviewContext context) {
        StringBuilder prompt = new StringBuilder();
        
        // System / Role Instructions
        prompt.append("You are an expert software engineer performing a code review.\n");
        prompt.append("Prompt Version: ").append(PROMPT_VERSION).append("\n\n");
        
        prompt.append("Please review the following pull request changes and provide a list of structured findings.\n");
        prompt.append("Title: ").append(context.title()).append("\n");
        if (context.description() != null && !context.description().isBlank()) {
            prompt.append("Description: ").append(context.description()).append("\n");
        }
        
        prompt.append("\nOutput your review STRICTLY as a JSON array of objects.\n");
        prompt.append("Return ONLY JSON.\n");
        prompt.append("Do not use markdown.\n");
        prompt.append("Do not explain your reasoning.\n");
        prompt.append("Do not wrap in ```json.\n");
        prompt.append("Do not include introductory text.\n");
        prompt.append("If there are no findings, return: []\n\n");
        
        prompt.append("Each object MUST match this exact schema:\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"file\": \"Exact filename as provided\",\n");
        prompt.append("    \"line\": integer line number (or null if the comment applies to the whole file),\n");
        prompt.append("    \"severity\": \"CRITICAL\" | \"MAJOR\" | \"MINOR\" | \"SUGGESTION\",\n");
        prompt.append("    \"category\": \"SECURITY\" | \"PERFORMANCE\" | \"BUG\" | \"BEST_PRACTICE\" | \"STYLE\",\n");
        prompt.append("    \"comment\": \"Detailed review comment explaining the issue and suggesting a fix\"\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        
        prompt.append("Here are the changed files:\n\n");
        
        for (ChangedFileContext file : context.files()) {
            // Include only changed files, skipping binary files that lack a patch
            if (file.isBinary() || file.patch() == null) {
                prompt.append("File: ").append(file.filename()).append(" (Binary/Large - omitted from review)\n\n");
                continue;
            }
            
            prompt.append("File: ").append(file.filename()).append("\n");
            prompt.append("Language: ").append(file.language()).append("\n");
            prompt.append("Change Type: ").append(file.changeType()).append("\n");
            prompt.append("```diff\n");
            prompt.append(file.patch()).append("\n");
            prompt.append("```\n\n");
        }
        
        return prompt.toString();
    }
}
