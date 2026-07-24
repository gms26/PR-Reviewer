package com.prreviewer.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewCommentMapperTest {

    private ReviewCommentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ReviewCommentMapper(new CommentFormatter(), new ReviewCommentValidator());
    }

    @Test
    void testNullOrEmptyFindings() {
        assertTrue(mapper.mapToComments(null).isEmpty());
        assertTrue(mapper.mapToComments(List.of()).isEmpty());
    }

    @Test
    void testValidFindingMappedCorrectly() {
        ReviewFinding finding = new ReviewFinding("src/Main.java", 10, Severity.MAJOR, Category.BUG, "Null pointer check missing.");
        
        List<ReviewComment> comments = mapper.mapToComments(List.of(finding));
        
        assertEquals(1, comments.size());
        ReviewComment comment = comments.get(0);
        assertEquals("src/Main.java", comment.path());
        assertEquals(10, comment.line());
        assertEquals("[MAJOR][BUG]\n\nNull pointer check missing.", comment.body());
    }

    @Test
    void testMissingSeverityAndCategoryFormatting() {
        ReviewFinding finding = new ReviewFinding("src/Utils.java", 5, null, null, "Needs refactoring.");
        
        List<ReviewComment> comments = mapper.mapToComments(List.of(finding));
        
        assertEquals(1, comments.size());
        assertEquals("Needs refactoring.", comments.get(0).body());
    }

    @Test
    void testSkipInvalidFindings() {
        ReviewFinding valid = new ReviewFinding("src/Main.java", 10, null, null, "Valid comment");
        ReviewFinding missingPath = new ReviewFinding("", 10, null, null, "Missing path");
        ReviewFinding nullLine = new ReviewFinding("src/Main.java", null, null, null, "Null line");
        ReviewFinding negativeLine = new ReviewFinding("src/Main.java", -1, null, null, "Negative line");
        ReviewFinding emptyBody = new ReviewFinding("src/Main.java", 10, null, null, "");

        List<ReviewComment> comments = mapper.mapToComments(List.of(valid, missingPath, nullLine, negativeLine, emptyBody));
        
        // Scenario 7: 5 findings -> 4 invalid skipped -> 1 valid left
        assertEquals(1, comments.size());
        assertEquals("src/Main.java", comments.get(0).path());
    }
}
