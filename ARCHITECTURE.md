# Architecture Overview

The PR Reviewer application is designed with a clean, decoupled architecture separating the fast, synchronous webhook ingestion from the slow, asynchronous AI review pipeline.

## System Components

### 1. Webhook Ingestion (Synchronous)
- **WebhookReceiverService**: Validates HMAC signatures, checks for duplicate deliveries, and persists the raw `WebhookDelivery`.
- **PullRequestService**: Extracts essential metadata and persists the `PullRequest` and `PullRequestEvent` entities in a transactional boundary. Returns immediately to GitHub with HTTP 200.

### 2. Review Pipeline (Asynchronous)
- **AsyncReviewCoordinatorService**: The thin orchestrator that executes the pipeline in a protected background thread (`@Async`). It guarantees no exceptions leak and produces a single unified completion log.
- **GitHubPullRequestService**: Fetches rich context (changed files, diffs) from the GitHub API using the user's OAuth token.
- **ContextBuilder**: Deterministically converts GitHub DTOs into a pure `ReviewContext` domain model, identifying file extensions and categorizing files.
- **AIReviewService**: Uses Gemini to generate code reviews based on the context.
- **ReviewCommentMapper**: Validates and maps the AI's `ReviewFinding` models into pure `ReviewComment` domain models, utilizing `CommentFormatter` and `ReviewCommentValidator`.
- **GitHubReviewCommentService**: Translates domain comments into the `GitHubReviewRequestDto` transport payload and posts a single batch review via `GitHubService`.

## Domain vs Infrastructure
The application strictly isolates the core domain (AI findings, code review context) from the infrastructure (GitHub API payloads, Gemini JSON responses). Mappers (like `ReviewCommentMapper`) act as translation layers to ensure this boundary is never breached.
