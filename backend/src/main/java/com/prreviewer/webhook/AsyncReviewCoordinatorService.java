package com.prreviewer.webhook;

import com.prreviewer.github.GitHubPullRequestService;
import com.prreviewer.github.PullRequestData;
import com.prreviewer.model.PullRequest;
import com.prreviewer.review.ContextBuilder;
import com.prreviewer.review.ReviewComment;
import com.prreviewer.review.ReviewCommentMapper;
import com.prreviewer.review.ReviewContext;
import com.prreviewer.review.ReviewFinding;
import com.prreviewer.service.AIReviewService;
import com.prreviewer.github.GitHubReviewCommentService;
import com.prreviewer.repository.PullRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Handles the long-running, asynchronous portions of the webhook processing pipeline.
 *
 * <p>Because GitHub expects webhooks to return an HTTP 200 within 10 seconds, this
 * component decouples the fast, synchronous persistence steps (HMAC validation,
 * metadata extraction) from the slow, I/O bound steps (GitHub fetching, Gemini AI
 * generation).
 */
@Service
public class AsyncReviewCoordinatorService {

    private static final Logger log = LoggerFactory.getLogger(AsyncReviewCoordinatorService.class);

    private final GitHubPullRequestService gitHubPullRequestService;
    private final ContextBuilder contextBuilder;
    private final AIReviewService aiReviewService;
    private final ReviewCommentMapper reviewCommentMapper;
    private final GitHubReviewCommentService gitHubReviewCommentService;
    private final PullRequestRepository pullRequestRepository;

    public AsyncReviewCoordinatorService(GitHubPullRequestService gitHubPullRequestService,
                                         ContextBuilder contextBuilder,
                                         AIReviewService aiReviewService,
                                         ReviewCommentMapper reviewCommentMapper,
                                         GitHubReviewCommentService gitHubReviewCommentService,
                                         PullRequestRepository pullRequestRepository) {
        this.gitHubPullRequestService = gitHubPullRequestService;
        this.contextBuilder = contextBuilder;
        this.aiReviewService = aiReviewService;
        this.reviewCommentMapper = reviewCommentMapper;
        this.gitHubReviewCommentService = gitHubReviewCommentService;
        this.pullRequestRepository = pullRequestRepository;
    }

    /**
     * Executes the GitHub fetch and AI review pipeline in a background thread.
     *
     * @param pullRequestId the ID of the pull request entity persisted in the synchronous phase
     */
    @Async("reviewExecutor")
    public void executeBackgroundReview(Long pullRequestId) {
        Instant start = Instant.now();
        String stage = "Initialization";
        PullRequest pr = null;

        try {
            pr = pullRequestRepository.findById(pullRequestId)
                    .orElseThrow(() -> new IllegalArgumentException("PullRequest not found for id: " + pullRequestId));

            log.info("Starting background review pipeline for PR id={} (repo={}/{})",
                    pr.getId(), pr.getRepository().getOwner(), pr.getRepository().getName());

            // 1. Fetch rich PR data from GitHub API
            stage = "GitHubFetch";
            PullRequestData data = gitHubPullRequestService.fetchAllPullRequestData(
                    pr.getRepository().getUser(),
                    pr.getRepository(),
                    pr.getGithubPrNumber()
            );

            // 2. Build the deterministic review context
            stage = "ContextBuilder";
            ReviewContext context = contextBuilder.build(data);

            // 3. Orchestrate the AI review
            stage = "AIReviewService";
            List<ReviewFinding> findings = aiReviewService.performReview(context);

            // 4. Map findings to infrastructure-agnostic review comments
            stage = "ReviewCommentMapper";
            List<ReviewComment> comments = reviewCommentMapper.mapToComments(findings);
            
            // 5. Submit the batch review to GitHub
            stage = "GitHubReviewCommentService";
            gitHubReviewCommentService.submitReview(pr.getRepository(), pr, comments);

            log.info("repository={}/{} pr={} status=SUCCESS findings={} commentsPosted={} durationMs={}",
                    pr.getRepository().getOwner(), pr.getRepository().getName(),
                    pr.getGithubPrNumber(), findings.size(), comments.size(),
                    Duration.between(start, Instant.now()).toMillis());

        } catch (Exception e) {
            String repoInfo = pr != null ? pr.getRepository().getOwner() + "/" + pr.getRepository().getName() : "unknown";
            Integer prNum = pr != null ? pr.getGithubPrNumber() : null;

            log.error("repository={} pr={} status=FAILED stage={} durationMs={}",
                    repoInfo, prNum, stage, Duration.between(start, Instant.now()).toMillis(), e);
        }
    }
}
