package com.prreviewer.review;

/**
 * An immutable snapshot of a single changed file within a Pull Request,
 * as prepared by {@link ContextBuilder}.
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><strong>Immutable</strong> — Java 21 record; no setters.</li>
 *   <li><strong>Deterministic</strong> — constructed entirely from
 *       {@link com.prreviewer.github.GitHubPullRequestFileDto} fields;
 *       no randomness, no timestamps.</li>
 *   <li><strong>AI-independent</strong> — carries raw data only;
 *       prompt construction belongs in Milestone 9.</li>
 * </ul>
 *
 * <h2>Language detection</h2>
 * <p>{@code language} is detected purely from {@code filename} by
 * {@link ContextBuilder#detectLanguage(String)}, following this order:
 * <ol>
 *   <li>Exact, case-sensitive match against known extensionless filenames
 *       ({@code Dockerfile}, {@code Makefile}, {@code Jenkinsfile},
 *       {@code Containerfile}).</li>
 *   <li>Extract the substring after the last {@code '.'} in the filename.</li>
 *   <li>Normalize the extension to lowercase using {@code Locale.ROOT}.</li>
 *   <li>Match against the known extension table (Java 21 switch expression).</li>
 *   <li>Return {@code "UNKNOWN"} for everything else.</li>
 * </ol>
 * <p>File contents, MIME type, and GitHub metadata are never inspected.
 *
 * <h2>Binary files</h2>
 * <p>When GitHub does not return a {@code patch} for a file (e.g. images,
 * compiled artifacts, or very large files), {@code isBinary} is set to
 * {@code true} and {@code patch} is {@code null}. This is not an error
 * condition — binary files must still appear in {@link ReviewContext#files()}.
 *
 * <h2>Change types</h2>
 * <p>{@code changeType} reflects the {@code status} field from the GitHub
 * API response verbatim: {@code "added"}, {@code "modified"}, {@code "removed"},
 * {@code "renamed"}, {@code "copied"}, {@code "changed"}, or {@code "unchanged"}.
 *
 * @param filename    The full file path as reported by GitHub
 *                    (e.g. {@code "src/main/java/com/example/Foo.java"})
 * @param language    Human-readable language name detected from {@code filename}
 *                    (e.g. {@code "Java"}, {@code "TypeScript"}, {@code "UNKNOWN"});
 *                    never {@code null}
 * @param changeType  The GitHub {@code status} field verbatim
 *                    (e.g. {@code "added"}, {@code "modified"}, {@code "removed"});
 *                    never {@code null}
 * @param additions   Number of lines added in this file; &ge; 0
 * @param deletions   Number of lines deleted in this file; &ge; 0
 * @param patch       The unified diff patch string for this file;
 *                    {@code null} when the file is binary or GitHub omitted the patch
 * @param isBinary    {@code true} when {@code patch} is {@code null} because the
 *                    file is binary or too large for GitHub to produce a diff;
 *                    {@code false} otherwise
 */
public record ChangedFileContext(
        String filename,
        String language,
        String changeType,
        int additions,
        int deletions,
        String patch,
        boolean isBinary
) {}
