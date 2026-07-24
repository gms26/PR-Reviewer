package com.prreviewer.review;

import java.util.List;

/**
 * An immutable, AI-independent snapshot of all information needed to review
 * a GitHub Pull Request.
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><strong>Immutable</strong> — backed by a Java 21 record; no setters.</li>
 *   <li><strong>Pure</strong> — carries data only; no methods that produce side effects.</li>
 *   <li><strong>Deterministic</strong> — given the same {@link com.prreviewer.github.PullRequestData},
 *       the same {@code ReviewContext} is produced every time.</li>
 *   <li><strong>AI-independent</strong> — no prompt text, no Gemini types, no token counts.</li>
 * </ul>
 *
 * <h2>Invariants</h2>
 * <p>All invariants are enforced by {@link ContextBuilder} at construction time:
 * <ul>
 *   <li>{@code repositoryOwner} is not blank.</li>
 *   <li>{@code repositoryName} is not blank.</li>
 *   <li>{@code pullRequestNumber} is &gt; 0.</li>
 *   <li>{@code title} is never {@code null}.</li>
 *   <li>{@code files} is never {@code null} and is sorted alphabetically by filename.</li>
 *   <li>{@code totalFiles} equals {@code files.size()}.</li>
 * </ul>
 *
 * <h2>Downstream usage (Milestone 9)</h2>
 * <p>The Gemini integration will receive a {@code ReviewContext} and use it to
 * construct a prompt string. This record must never be modified to accommodate
 * Gemini-specific concerns.
 *
 * @param repositoryOwner  GitHub login of the repository owner (e.g. {@code "octocat"})
 * @param repositoryName   Name of the repository (e.g. {@code "hello-world"})
 * @param repositoryFullName Full name in {@code owner/repo} format (e.g. {@code "octocat/hello-world"})
 * @param pullRequestNumber GitHub PR number; always &gt; 0
 * @param title            PR title; never {@code null}
 * @param description      PR body; may be empty but never {@code null}
 * @param baseBranch       The base (target) branch name (e.g. {@code "main"})
 * @param headBranch       The head (source) branch name (e.g. {@code "feature/login"})
 * @param baseSha          The base commit SHA
 * @param headSha          The HEAD commit SHA at the time of the webhook event
 * @param author           GitHub login of the PR author
 * @param totalFiles       Total number of changed files; equals {@code files.size()}
 * @param files            Sorted (alphabetically by filename), immutable list of changed files
 */
public record ReviewContext(
        String repositoryOwner,
        String repositoryName,
        String repositoryFullName,
        int pullRequestNumber,
        String title,
        String description,
        String baseBranch,
        String headBranch,
        String baseSha,
        String headSha,
        String author,
        int totalFiles,
        List<ChangedFileContext> files
) {}
