package com.prreviewer.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single repository object returned by the GitHub REST API
 * ({@code GET /user/repos}).
 *
 * <p>Only the fields this application needs are declared here. All other
 * fields in the GitHub JSON response are silently ignored via
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)}, which insulates
 * us from GitHub adding new fields to their API.
 *
 * <p>Naming: GitHub returns snake_case JSON. Jackson maps these to
 * Java fields via {@code @JsonProperty} annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepoDto {

    /** The GitHub-assigned numeric repository ID. Permanent and globally unique. */
    @JsonProperty("id")
    private Long id;

    /** The repository name (without the owner prefix), e.g. {@code "hello-world"}. */
    @JsonProperty("name")
    private String name;

    /**
     * The full repository name in {@code owner/repo} format,
     * e.g. {@code "octocat/hello-world"}.
     * Used directly to avoid repeatedly rebuilding this string in later milestones.
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * The owner of this repository.
     * Deserialized as a nested object; only the {@code login} field is used.
     */
    @JsonProperty("owner")
    private Owner owner;

    /** Whether this is a private repository. */
    @JsonProperty("private")
    private boolean privateRepo;

    /** Optional repository description. May be null. */
    @JsonProperty("description")
    private String description;

    /** The HTML URL of the repository on GitHub, e.g. {@code "https://github.com/octocat/hello-world"}. */
    @JsonProperty("html_url")
    private String htmlUrl;

    // ---- Getters ----

    public Long getId()            { return id; }
    public String getName()        { return name; }
    public String getFullName()    { return fullName; }
    public boolean isPrivateRepo() { return privateRepo; }
    public String getDescription() { return description; }
    public String getHtmlUrl()     { return htmlUrl; }

    /**
     * Returns the owner login (GitHub username) extracted from the nested
     * {@link Owner} object. Returns {@code null} if the owner field is absent.
     */
    public String getOwnerLogin() {
        return owner != null ? owner.login : null;
    }

    // ---- Nested owner object ----

    /**
     * Partial deserialization of the GitHub {@code owner} sub-object.
     * Only the {@code login} field is needed.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        @JsonProperty("login")
        private String login;

        public String getLogin() { return login; }
    }
}
