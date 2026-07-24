package com.prreviewer.review;

import com.prreviewer.github.GitHubPullRequestDto;
import com.prreviewer.github.GitHubPullRequestFileDto;
import com.prreviewer.github.PullRequestData;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Pure, stateless service responsible for transforming raw GitHub API responses
 * into an AI-independent, deterministic {@link ReviewContext}.
 *
 * <p>This service contains zero side effects. It makes no network calls,
 * interacts with no databases, and maintains no internal state. Given identical
 * {@link PullRequestData}, it is guaranteed to produce an identical
 * {@link ReviewContext}.
 */
@Service
public class ContextBuilder {

    /**
     * Constructs a deterministic, immutable {@link ReviewContext} from the given raw GitHub data.
     *
     * @param data the aggregated GitHub pull request data; must not be null
     * @return a clean, normalized review context
     * @throws IllegalArgumentException if the provided data or its core components are null
     */
    public ReviewContext build(PullRequestData data) {
        validate(data);

        GitHubPullRequestDto metadata = data.metadata();

        // Map and sort files
        List<ChangedFileContext> sortedFiles = data.files().stream()
                .map(this::mapFile)
                .sorted(Comparator.comparing(ChangedFileContext::filename))
                .toList(); // Java 16+ toList() returns an unmodifiable list

        String owner = extractOwner(metadata);
        String repo = extractRepo(metadata);

        return new ReviewContext(
                owner,
                repo,
                owner + "/" + repo,
                metadata.number(),
                metadata.title(),
                metadata.body() == null ? "" : metadata.body(), // description may be empty
                metadata.base() != null ? metadata.base().ref() : "",
                metadata.head() != null ? metadata.head().ref() : "",
                metadata.base() != null ? metadata.base().sha() : "",
                metadata.head() != null ? metadata.head().sha() : "",
                metadata.user() != null ? metadata.user().login() : "",
                sortedFiles.size(),
                sortedFiles
        );
    }

    private void validate(PullRequestData data) {
        if (data == null) {
            throw new IllegalArgumentException("PullRequestData cannot be null");
        }
        if (data.metadata() == null) {
            throw new IllegalArgumentException("PullRequestData.metadata cannot be null");
        }
        if (data.files() == null) {
            throw new IllegalArgumentException("PullRequestData.files cannot be null");
        }
        if (data.metadata().title() == null) {
            throw new IllegalArgumentException("PullRequest title cannot be null");
        }
        if (data.metadata().number() <= 0) {
            throw new IllegalArgumentException("PullRequest number must be > 0");
        }
    }

    private ChangedFileContext mapFile(GitHubPullRequestFileDto fileDto) {
        String filename = fileDto.filename();
        String language = detectLanguage(filename);
        String patch = fileDto.patch();
        boolean isBinary = patch == null;

        return new ChangedFileContext(
                filename,
                language,
                fileDto.status(),
                fileDto.additions(),
                fileDto.deletions(),
                patch,
                isBinary
        );
    }

    private String detectLanguage(String filename) {
        // Step 1 & 2: Special extensionless filenames (Exact, case-sensitive)
        switch (filename) {
            case "Dockerfile":
            case "Containerfile":
                return "Docker";
            case "Makefile":
                return "Make";
            case "Jenkinsfile":
                return "Groovy";
            default:
                break;
        }

        // Step 3: Extract extension
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            // No extension found, or ends with a dot
            return "UNKNOWN";
        }

        String extension = filename.substring(lastDotIndex + 1);

        // Step 4: Normalize to lowercase using Locale.ROOT
        String normalizedExtension = extension.toLowerCase(Locale.ROOT);

        // Step 5: Java 21 switch expression
        return switch (normalizedExtension) {
            case "java" -> "Java";
            case "kt" -> "Kotlin";
            case "js", "jsx" -> "JavaScript";
            case "ts", "tsx" -> "TypeScript";
            case "py" -> "Python";
            case "go" -> "Go";
            case "rs" -> "Rust";
            case "cpp", "cc", "cxx", "hpp", "h" -> "C++";
            case "c" -> "C";
            case "cs" -> "C#";
            case "php" -> "PHP";
            case "rb" -> "Ruby";
            case "swift" -> "Swift";
            case "html", "htm" -> "HTML";
            case "css" -> "CSS";
            case "scss" -> "SCSS";
            case "sql" -> "SQL";
            case "xml" -> "XML";
            case "yml", "yaml" -> "YAML";
            case "json" -> "JSON";
            case "md", "markdown" -> "Markdown";
            case "sh", "bash" -> "Shell";
            default -> "UNKNOWN"; // Step 6
        };
    }

    private String extractOwner(GitHubPullRequestDto metadata) {
        if (metadata.base() != null && metadata.base().repo() != null && metadata.base().repo().owner() != null) {
            return metadata.base().repo().owner().login();
        }
        return "unknown"; 
    }

    private String extractRepo(GitHubPullRequestDto metadata) {
        if (metadata.base() != null && metadata.base().repo() != null) {
            return metadata.base().repo().name();
        }
        return "unknown";
    }
}
