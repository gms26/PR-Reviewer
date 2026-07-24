package com.prreviewer.github;

import com.prreviewer.exception.GitHubApiException;
import com.prreviewer.exception.GitHubValidationException;
import com.prreviewer.model.PullRequest;
import com.prreviewer.model.Repository;
import com.prreviewer.model.User;
import com.prreviewer.review.ReviewComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GitHubReviewCommentServiceTest {

    private GitHubService gitHubService;
    private GitHubReviewCommentService service;
    private Repository repository;
    private PullRequest pullRequest;

    @BeforeEach
    void setUp() {
        gitHubService = mock(GitHubService.class);
        service = new GitHubReviewCommentService(gitHubService);

        User user = new User();
        user.setAccessToken("test-token");
        
        repository = new Repository();
        repository.setOwner("test-owner");
        repository.setName("test-repo");
        repository.setUser(user);

        pullRequest = new PullRequest();
        pullRequest.setGithubPrNumber(42);
        pullRequest.setHeadSha("abc123sha");
    }

    @Test
    void testSubmitReviewEmptyListDoesNothing() {
        service.submitReview(repository, pullRequest, List.of());
        verify(gitHubService, never()).postPullRequestReview(any(), any(), any(), anyInt(), any());
    }

    @Test
    void testSubmitReviewMissingHeadShaDoesNothing() {
        pullRequest.setHeadSha(null);
        service.submitReview(repository, pullRequest, List.of(new ReviewComment("test.java", 1, "body")));
        verify(gitHubService, never()).postPullRequestReview(any(), any(), any(), anyInt(), any());
    }

    @Test
    void testSubmitReviewSuccess() {
        List<ReviewComment> comments = List.of(
                new ReviewComment("src/Test.java", 10, "comment 1"),
                new ReviewComment("src/Main.java", 20, "comment 2")
        );

        service.submitReview(repository, pullRequest, comments);

        ArgumentCaptor<GitHubReviewRequestDto> captor = ArgumentCaptor.forClass(GitHubReviewRequestDto.class);
        verify(gitHubService).postPullRequestReview(eq("test-token"), eq("test-owner"), eq("test-repo"), eq(42), captor.capture());

        GitHubReviewRequestDto request = captor.getValue();
        assertEquals("abc123sha", request.commitId());
        assertEquals("COMMENT", request.event());
        assertEquals(2, request.comments().size());
        
        assertEquals("src/Test.java", request.comments().get(0).path());
        assertEquals(10, request.comments().get(0).line());
        assertEquals("RIGHT", request.comments().get(0).side());
        assertEquals("comment 1", request.comments().get(0).body());
    }

    @Test
    void testSubmitReviewHandlesExceptionsGracefully() {
        List<ReviewComment> comments = List.of(new ReviewComment("src/Test.java", 10, "comment 1"));
        
        // Mock throwing a 422 validation exception
        doThrow(new GitHubValidationException("422 unprocessable"))
                .when(gitHubService).postPullRequestReview(anyString(), anyString(), anyString(), anyInt(), any());

        assertDoesNotThrow(() -> service.submitReview(repository, pullRequest, comments));
        
        // Mock throwing a generic API exception
        doThrow(new GitHubApiException("503 server error"))
                .when(gitHubService).postPullRequestReview(anyString(), anyString(), anyString(), anyInt(), any());

        assertDoesNotThrow(() -> service.submitReview(repository, pullRequest, comments));
    }
}
