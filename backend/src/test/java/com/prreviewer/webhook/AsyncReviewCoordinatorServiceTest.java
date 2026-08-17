package com.prreviewer.webhook;

import com.prreviewer.github.GitHubPullRequestService;
import com.prreviewer.github.PullRequestData;
import com.prreviewer.github.GitHubReviewCommentService;
import com.prreviewer.model.PullRequest;
import com.prreviewer.model.Repository;
import com.prreviewer.model.User;
import com.prreviewer.repository.PullRequestRepository;
import com.prreviewer.review.ContextBuilder;
import com.prreviewer.review.ReviewComment;
import com.prreviewer.review.ReviewCommentMapper;
import com.prreviewer.review.ReviewContext;
import com.prreviewer.review.ReviewFinding;
import com.prreviewer.service.AIReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AsyncReviewCoordinatorServiceTest {

    private AsyncReviewCoordinatorService coordinator;

    private GitHubPullRequestService gitHubPullRequestService;
    private ContextBuilder contextBuilder;
    private AIReviewService aiReviewService;
    private ReviewCommentMapper reviewCommentMapper;
    private GitHubReviewCommentService gitHubReviewCommentService;
    private PullRequestRepository pullRequestRepository;

    private PullRequest pullRequest;
    private Repository repository;

    @BeforeEach
    void setUp() {
        gitHubPullRequestService = mock(GitHubPullRequestService.class);
        contextBuilder = mock(ContextBuilder.class);
        aiReviewService = mock(AIReviewService.class);
        reviewCommentMapper = mock(ReviewCommentMapper.class);
        gitHubReviewCommentService = mock(GitHubReviewCommentService.class);
        pullRequestRepository = mock(PullRequestRepository.class);

        coordinator = new AsyncReviewCoordinatorService(
                gitHubPullRequestService,
                contextBuilder,
                aiReviewService,
                reviewCommentMapper,
                gitHubReviewCommentService,
                pullRequestRepository
        );

        User user = new User();
        repository = new Repository();
        repository.setOwner("owner");
        repository.setName("repo");
        repository.setUser(user);

        pullRequest = new PullRequest();
        ReflectionTestUtils.setField(pullRequest, "id", 100L);
        pullRequest.setGithubPrNumber(42);
        pullRequest.setRepository(repository);
    }

    @Test
    void testExecuteBackgroundReviewSuccessPath() {
        when(pullRequestRepository.findByIdWithRepositoryAndUser(100L)).thenReturn(Optional.of(pullRequest));

        PullRequestData prData = mock(PullRequestData.class);
        when(gitHubPullRequestService.fetchAllPullRequestData(any(), any(), eq(42))).thenReturn(prData);

        ReviewContext context = mock(ReviewContext.class);
        when(contextBuilder.build(prData)).thenReturn(context);

        List<ReviewFinding> findings = List.of(mock(ReviewFinding.class));
        when(aiReviewService.performReview(context)).thenReturn(findings);

        List<ReviewComment> comments = List.of(mock(ReviewComment.class));
        when(reviewCommentMapper.mapToComments(findings)).thenReturn(comments);

        assertDoesNotThrow(() -> coordinator.executeBackgroundReview(100L));

        InOrder inOrder = inOrder(
                pullRequestRepository, 
                gitHubPullRequestService, 
                contextBuilder, 
                aiReviewService, 
                reviewCommentMapper, 
                gitHubReviewCommentService
        );

        inOrder.verify(pullRequestRepository).findByIdWithRepositoryAndUser(100L);
        inOrder.verify(gitHubPullRequestService).fetchAllPullRequestData(any(), eq(repository), eq(42));
        inOrder.verify(contextBuilder).build(prData);
        inOrder.verify(aiReviewService).performReview(context);
        inOrder.verify(reviewCommentMapper).mapToComments(findings);
        inOrder.verify(gitHubReviewCommentService).submitReview(repository, pullRequest, comments);
    }

    @Test
    void testExecuteBackgroundReviewHandlesExceptionsGracefully() {
        when(pullRequestRepository.findByIdWithRepositoryAndUser(100L)).thenReturn(Optional.of(pullRequest));
        when(gitHubPullRequestService.fetchAllPullRequestData(any(), any(), anyInt()))
                .thenThrow(new RuntimeException("API failure"));

        assertDoesNotThrow(() -> coordinator.executeBackgroundReview(100L));

        verify(contextBuilder, never()).build(any());
        verify(aiReviewService, never()).performReview(any());
    }
}
