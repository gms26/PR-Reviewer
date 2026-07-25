package com.prreviewer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler.
 *
 * <p>Catches all exceptions before they reach the HTTP response layer.
 * Returns RFC 7807 Problem Detail responses so the frontend can
 * display meaningful errors without exposing internal stack traces.
 *
 * <p><strong>Security:</strong> Never include stack traces, internal messages,
 * or sensitive fields in the response body.
 */
@RestControllerAdvice(basePackages = "com.prreviewer")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    // Domain exceptions
    // ----------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildProblem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(RepositoryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRepositoryNotFound(RepositoryNotFoundException ex) {
        log.warn("Repository not found: repositoryId={}", ex.getRepositoryId());
        return buildProblem(HttpStatus.NOT_FOUND, "Not Found", "Repository not found");
    }

    @ExceptionHandler(WebhookSignatureException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSignature(WebhookSignatureException ex) {
        // Log at WARN — invalid signatures can indicate probing attacks
        log.warn("Webhook signature verification failed: {}", ex.getMessage());
        return buildProblem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid webhook signature");
    }

    @ExceptionHandler(DuplicateWebhookDeliveryException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(DuplicateWebhookDeliveryException ex) {
        log.debug("Duplicate webhook delivery ignored: {}", ex.getMessage());
        return buildProblem(HttpStatus.OK, "Already Processed", "Webhook delivery already processed");
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ProblemDetail> handleGitHubApi(GitHubApiException ex) {
        log.error("GitHub API error: {}", ex.getMessage());
        return buildProblem(HttpStatus.BAD_GATEWAY, "GitHub API Error", "Failed to communicate with GitHub");
    }

    @ExceptionHandler(GitHubRateLimitException.class)
    public ResponseEntity<ProblemDetail> handleGitHubRateLimit(GitHubRateLimitException ex) {
        // WARN level — this is operationally significant (GitHub API quota exhausted).
        // Log the full message so the reset timestamp is visible in logs.
        log.warn("GitHub API rate limit hit: {}", ex.getMessage());
        return buildProblem(HttpStatus.SERVICE_UNAVAILABLE, "GitHub Rate Limit Exceeded",
                "GitHub API rate limit reached. Please try again later.");
    }

    @ExceptionHandler(RepositoryOwnershipException.class)
    public ResponseEntity<ProblemDetail> handleOwnership(RepositoryOwnershipException ex) {
        // WARN level — failed ownership checks can indicate ID-guessing attempts.
        log.warn("Repository ownership validation failed: {}", ex.getMessage());
        return buildProblem(HttpStatus.FORBIDDEN, "Forbidden", "Repository not accessible");
    }

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ProblemDetail> handleGeminiApi(GeminiApiException ex) {
        log.error("Gemini API error: {}", ex.getMessage());
        return buildProblem(HttpStatus.BAD_GATEWAY, "AI Service Error", "Failed to communicate with AI service");
    }

    // ----------------------------------------------------------------
    // Spring Security exceptions
    // ----------------------------------------------------------------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        log.debug("Authentication required");
        return buildProblem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication required");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Access denied");
        return buildProblem(HttpStatus.FORBIDDEN, "Forbidden", "Access denied");
    }

    // ----------------------------------------------------------------
    // Validation exceptions
    // ----------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildProblem(HttpStatus.BAD_REQUEST, "Validation Failed", detail);
    }

    // ----------------------------------------------------------------
    // Catch-all
    // ----------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.");
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private ResponseEntity<ProblemDetail> buildProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(problem);
    }
}
