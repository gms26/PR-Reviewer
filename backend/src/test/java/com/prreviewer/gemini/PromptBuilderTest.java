package com.prreviewer.gemini;

import com.prreviewer.review.ChangedFileContext;
import com.prreviewer.review.ReviewContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
    }

    @Test
    void shouldIncludePromptVersionAndSchemaInstructions() {
        ReviewContext context = new ReviewContext(
                "octocat", "hello-world", "octocat/hello-world",
                1, "Fix tests", "description", "main", "feature", "sha1", "sha2", "alice", 0, List.of()
        );

        String prompt = promptBuilder.buildPrompt(context);

        assertThat(prompt).contains("Prompt Version: v1");
        assertThat(prompt).contains("Title: Fix tests");
        assertThat(prompt).contains("Description: description");
        assertThat(prompt).contains("\"file\": \"Exact filename as provided\"");
    }

    @Test
    void shouldIncludeChangedFilesButOmitBinaryFilePatches() {
        ChangedFileContext normalFile = new ChangedFileContext(
                "src/Foo.java", "Java", "modified", 10, 2, "@@ -1 +1 @@", false
        );
        ChangedFileContext binaryFile = new ChangedFileContext(
                "logo.png", "UNKNOWN", "added", 0, 0, null, true
        );
        
        ReviewContext context = new ReviewContext(
                "octocat", "hello-world", "octocat/hello-world",
                1, "Fix tests", "", "main", "feature", "sha1", "sha2", "alice", 2, List.of(normalFile, binaryFile)
        );

        String prompt = promptBuilder.buildPrompt(context);

        assertThat(prompt).contains("File: src/Foo.java");
        assertThat(prompt).contains("```diff\n@@ -1 +1 @@\n```");
        
        assertThat(prompt).contains("File: logo.png (Binary/Large - omitted from review)");
        assertThat(prompt).doesNotContain("Language: UNKNOWN\nChange Type: added\n```diff\nnull");
    }
}
