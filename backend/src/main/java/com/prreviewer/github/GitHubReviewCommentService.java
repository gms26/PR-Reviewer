package com.prreviewer.github;

import com.prreviewer.exception.GitHubApiException;
import com.prreviewer.exception.GitHubValidationException;
import com.prreviewer.model.PullRequest;
import com.prreviewer.model.Repository;
import com.prreviewer.review.ReviewComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the submission of review comments to GitHub via the Batch Reviews API.
 * This service explicitly limits its responsibilities to transport-level concerns:
 * mapping internal comments to GitHub DTOs, attaching PR-specific metadata (headSha, side),
 * and logging API failures without modifying the pure domain models.
 */
@Service
public class GitHubReviewCommentService {

    private static final Logger log = LoggerFactory.getLogger(GitHubReviewCommentService.class);

    private static final String SIDE_RIGHT = "RIGHT";
    private static final String REVIEW_EVENT_COMMENT = "COMMENT";

    private final GitHubService gitHubService;

    public GitHubReviewCommentService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    /**
     * Submits a cohesive batch review to the GitHub API.
     * Aborts immediately if the list of comments is empty.
     * Failed reviews are logged but do not throw fatal exceptions (retries deferred to Phase 2).
     *
     * @param repository  the repository the PR belongs to
     * @param pullRequest the pull request to review (used for headSha anchoring)
     * @param comments    the pure, validated internal review comments
     */
    public void submitReview(Repository repository, PullRequest pullRequest, List<ReviewComment> comments) {
        if (pullRequest.getHeadSha() == null || pullRequest.getHeadSha().isBlank()) {
            log.error("Cannot post review for PR #{}: missing head SHA.", pullRequest.getGithubPrNumber());
            return;
        }

        if (comments == null || comments.isEmpty()) {
            log.info("No valid review findings to post for PR {}/{}. Returning successfully.", 
                     repository.getOwner(), repository.getName());
            return;
        }

        long start = System.currentTimeMillis();

        List<GitHubReviewCommentDto> commentDtos = comments.stream()
                .map(c -> new GitHubReviewCommentDto(c.path(), c.line(), SIDE_RIGHT, c.body()))
                .toList();

        GitHubReviewRequestDto payload = new GitHubReviewRequestDto(
                pullRequest.getHeadSha(),
                REVIEW_EVENT_COMMENT,
                commentDtos
        );

        try {
            gitHubService.postPullRequestReview(
                    repository.getUser().getAccessToken(),
                    repository.getOwner(),
                    repository.getName(),
                    pullRequest.getGithubPrNumber(),
                    payload
            );
            
            log.info("Review posted repository={}/{} pr={} comments={} durationMs={}",
                    repository.getOwner(), repository.getName(), 
                    pullRequest.getGithubPrNumber(), 
                    commentDtos.size(), 
                    System.currentTimeMillis() - start);

        } catch (GitHubValidationException ex) {
            log.error("GitHub rejected review payload for {}/{} #{} (422 Unprocessable Entity). " +
                      "This typically means a comment was anchored to an invalid line or a file not in the diff.",
                      repository.getOwner(), repository.getName(), pullRequest.getGithubPrNumber());
        } catch (GitHubApiException ex) {
            log.error("Failed to post review to {}/{} #{} due to transport or infrastructure error.",
                      repository.getOwner(), repository.getName(), pullRequest.getGithubPrNumber(), ex);
        }
    }
}
