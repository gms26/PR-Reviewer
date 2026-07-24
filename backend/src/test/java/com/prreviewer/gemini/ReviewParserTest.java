package com.prreviewer.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prreviewer.exception.ReviewParseException;
import com.prreviewer.review.Category;
import com.prreviewer.review.ReviewFinding;
import com.prreviewer.review.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewParserTest {

    private ReviewParser parser;

    @BeforeEach
    void setUp() {
        parser = new ReviewParser(new ObjectMapper());
    }

    @Test
    void shouldParseValidJson() {
        String json = """
                [
                  {
                    "file": "src/Foo.java",
                    "line": 42,
                    "severity": "MAJOR",
                    "category": "BUG",
                    "comment": "Null pointer possible"
                  }
                ]
                """;

        List<ReviewFinding> findings = parser.parse(json);

        assertThat(findings).hasSize(1);
        ReviewFinding f = findings.get(0);
        assertThat(f.file()).isEqualTo("src/Foo.java");
        assertThat(f.line()).isEqualTo(42);
        assertThat(f.severity()).isEqualTo(Severity.MAJOR);
        assertThat(f.category()).isEqualTo(Category.BUG);
    }

    @Test
    void shouldParseMarkdownWrappedJson() {
        String json = """
                ```json
                [
                  {
                    "file": "README.md",
                    "line": null,
                    "severity": "SUGGESTION",
                    "category": "STYLE",
                    "comment": "Fix typo"
                  }
                ]
                ```
                """;

        List<ReviewFinding> findings = parser.parse(json);

        assertThat(findings).hasSize(1);
        assertThat(findings.get(0).file()).isEqualTo("README.md");
        assertThat(findings.get(0).line()).isNull();
        assertThat(findings.get(0).severity()).isEqualTo(Severity.SUGGESTION);
    }

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
        assertThat(parser.parse("   ")).isEmpty();
        assertThat(parser.parse("[]")).isEmpty();
    }

    @Test
    void shouldThrowExceptionOnMalformedJson() {
        String json = "This is not json";

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(ReviewParseException.class)
                .hasMessageContaining("Failed to parse AI response");
    }

    @Test
    void shouldThrowExceptionOnUnknownEnum() {
        String json = """
                [
                  {
                    "file": "src/Foo.java",
                    "severity": "SUPER_CRITICAL",
                    "category": "BUG",
                    "comment": "Uh oh"
                  }
                ]
                """;

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(ReviewParseException.class);
    }
}
