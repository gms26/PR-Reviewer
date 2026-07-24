package com.prreviewer.review;

import com.prreviewer.github.GitHubPullRequestDto;
import com.prreviewer.github.GitHubPullRequestFileDto;
import com.prreviewer.github.PullRequestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContextBuilderTest {

    private ContextBuilder builder;
    private GitHubPullRequestDto validMetadata;

    @BeforeEach
    void setUp() {
        builder = new ContextBuilder();

        // Setup base valid metadata for tests
        GitHubPullRequestDto.UserRef owner = new GitHubPullRequestDto.UserRef("octocat");
        GitHubPullRequestDto.RepoRef repo = new GitHubPullRequestDto.RepoRef("hello-world", owner);
        GitHubPullRequestDto.CommitRef base = new GitHubPullRequestDto.CommitRef("baseSha", "main", repo);
        GitHubPullRequestDto.CommitRef head = new GitHubPullRequestDto.CommitRef("headSha", "feature", repo);
        GitHubPullRequestDto.UserRef author = new GitHubPullRequestDto.UserRef("alice");

        validMetadata = new GitHubPullRequestDto(
                42,
                "Fix all bugs",
                "This PR fixes everything.",
                "open",
                head,
                base,
                author
        );
    }

    @Test
    void shouldBuildReviewContextCorrectly() {
        // Given
        List<GitHubPullRequestFileDto> files = List.of(
                new GitHubPullRequestFileDto("src/Foo.java", "modified", 10, 2, 12, "@@ -1 +1 @@"),
                new GitHubPullRequestFileDto("README.md", "modified", 5, 0, 5, "@@ -1 +1 @@")
        );
        PullRequestData data = new PullRequestData(validMetadata, files, "raw diff", Instant.now());

        // When
        ReviewContext context = builder.build(data);

        // Then
        assertThat(context.repositoryOwner()).isEqualTo("octocat");
        assertThat(context.repositoryName()).isEqualTo("hello-world");
        assertThat(context.repositoryFullName()).isEqualTo("octocat/hello-world");
        assertThat(context.pullRequestNumber()).isEqualTo(42);
        assertThat(context.title()).isEqualTo("Fix all bugs");
        assertThat(context.author()).isEqualTo("alice");
        assertThat(context.totalFiles()).isEqualTo(2);
        assertThat(context.files()).hasSize(2);

        // Files should be sorted alphabetically: README.md, then src/Foo.java
        assertThat(context.files().get(0).filename()).isEqualTo("README.md");
        assertThat(context.files().get(0).language()).isEqualTo("Markdown");

        assertThat(context.files().get(1).filename()).isEqualTo("src/Foo.java");
        assertThat(context.files().get(1).language()).isEqualTo("Java");
    }

    @Test
    void shouldDetectLanguagesCorrectly() {
        // Given files covering all edge cases
        List<GitHubPullRequestFileDto> files = List.of(
                file("UserService.java"),            // Normal Java
                file("component.test.js"),           // Multiple dots
                file("Dockerfile"),                  // Extensionless special
                file("Containerfile"),               // Extensionless special alias
                file("Makefile"),                    // Extensionless special
                file("Jenkinsfile"),                 // Extensionless special
                file("LICENSE"),                     // Extensionless unknown
                file("Foo.JAVA"),                    // Uppercase extension
                file("script.ts"),                   // TypeScript
                file("styles.scss"),                 // SCSS
                file("no_extension.")                // Ends with dot
        );
        PullRequestData data = new PullRequestData(validMetadata, files, "raw diff", Instant.now());

        // When
        ReviewContext context = builder.build(data);
        List<ChangedFileContext> resultFiles = context.files();

        // Then (Order depends on alphabetical sorting)
        assertThat(findLanguage(resultFiles, "Containerfile")).isEqualTo("Docker");
        assertThat(findLanguage(resultFiles, "Dockerfile")).isEqualTo("Docker");
        assertThat(findLanguage(resultFiles, "Foo.JAVA")).isEqualTo("Java");
        assertThat(findLanguage(resultFiles, "Jenkinsfile")).isEqualTo("Groovy");
        assertThat(findLanguage(resultFiles, "LICENSE")).isEqualTo("UNKNOWN");
        assertThat(findLanguage(resultFiles, "Makefile")).isEqualTo("Make");
        assertThat(findLanguage(resultFiles, "UserService.java")).isEqualTo("Java");
        assertThat(findLanguage(resultFiles, "component.test.js")).isEqualTo("JavaScript");
        assertThat(findLanguage(resultFiles, "no_extension.")).isEqualTo("UNKNOWN");
        assertThat(findLanguage(resultFiles, "script.ts")).isEqualTo("TypeScript");
        assertThat(findLanguage(resultFiles, "styles.scss")).isEqualTo("SCSS");
    }

    @Test
    void shouldFlagBinaryFilesWhenPatchIsNull() {
        // Given
        List<GitHubPullRequestFileDto> files = List.of(
                new GitHubPullRequestFileDto("image.png", "added", 0, 0, 0, null),
                new GitHubPullRequestFileDto("script.sh", "modified", 5, 0, 5, "@@ -1 +1 @@")
        );
        PullRequestData data = new PullRequestData(validMetadata, files, "raw diff", Instant.now());

        // When
        ReviewContext context = builder.build(data);

        // Then
        ChangedFileContext img = context.files().stream().filter(f -> f.filename().equals("image.png")).findFirst().get();
        assertThat(img.isBinary()).isTrue();
        assertThat(img.patch()).isNull();

        ChangedFileContext script = context.files().stream().filter(f -> f.filename().equals("script.sh")).findFirst().get();
        assertThat(script.isBinary()).isFalse();
        assertThat(script.patch()).isNotNull();
    }

    @Test
    void shouldFailFastOnInvalidInput() {
        assertThatThrownBy(() -> builder.build(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PullRequestData cannot be null");

        PullRequestData missingMetadata = new PullRequestData(null, List.of(), "diff", Instant.now());
        assertThatThrownBy(() -> builder.build(missingMetadata))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PullRequestData.metadata cannot be null");

        PullRequestData missingFiles = new PullRequestData(validMetadata, null, "diff", Instant.now());
        assertThatThrownBy(() -> builder.build(missingFiles))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PullRequestData.files cannot be null");

        GitHubPullRequestDto badMetadata = new GitHubPullRequestDto(
                0, null, null, null, null, null, null
        );
        PullRequestData badData = new PullRequestData(badMetadata, List.of(), "diff", Instant.now());
        assertThatThrownBy(() -> builder.build(badData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be null");
    }

    private GitHubPullRequestFileDto file(String filename) {
        return new GitHubPullRequestFileDto(filename, "modified", 1, 1, 2, "@@ -1 +1 @@");
    }

    private String findLanguage(List<ChangedFileContext> files, String filename) {
        return files.stream()
                .filter(f -> f.filename().equals(filename))
                .findFirst()
                .map(ChangedFileContext::language)
                .orElseThrow(() -> new AssertionError("File not found: " + filename));
    }
}
