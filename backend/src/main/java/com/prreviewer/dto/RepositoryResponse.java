package com.prreviewer.dto;

import com.prreviewer.github.GitHubRepoDto;
import com.prreviewer.model.Repository;

/**
 * Public API representation of a GitHub repository.
 *
 * <p>Returned by both:
 * <ul>
 *   <li>{@code GET /repos} — the full list; {@code selected} reflects whether
 *       the user has already chosen to monitor this repository.</li>
 *   <li>{@code POST /repos/select} — the newly persisted repository, with
 *       {@code selected = true} and the current {@code webhookEnabled} state.
 *       Returning the full object here lets the frontend update its local state
 *       without issuing a follow-up GET.</li>
 * </ul>
 *
 * <p>Two static factory methods cover the two use-cases:
 * <ul>
 *   <li>{@link #from(GitHubRepoDto, boolean, Long, boolean)} — for the list endpoint,
 *       where entity state (id, webhookEnabled) is passed alongside GitHub API data.</li>
 *   <li>{@link #from(Repository)} — for mutating endpoints (select, enable, disable),
 *       where the full entity is available.</li>
 * </ul>
 */
public final class RepositoryResponse {

    private final Long    id;           // internal DB primary key; null for unselected repos
    private final Long    githubRepoId;
    private final String  owner;
    private final String  name;
    private final String  fullName;
    private final String  description;
    private final boolean privateRepo;
    private final String  htmlUrl;
    private final boolean selected;
    private final boolean webhookEnabled;
    private final Long    webhookId;

    private RepositoryResponse(Long id, Long githubRepoId, String owner, String name,
                                String fullName, String description, boolean privateRepo,
                                String htmlUrl, boolean selected, boolean webhookEnabled,
                                Long webhookId) {
        this.id             = id;
        this.githubRepoId   = githubRepoId;
        this.owner          = owner;
        this.name           = name;
        this.fullName       = fullName;
        this.description    = description;
        this.privateRepo    = privateRepo;
        this.htmlUrl        = htmlUrl;
        this.selected       = selected;
        this.webhookEnabled = webhookEnabled;
        this.webhookId      = webhookId;
    }

    /**
     * Creates a response from a GitHub API DTO plus entity state for selected repositories.
     * Used by {@code GET /repos}.
     *
     * <p>Unlike entity-backed responses, description, privateRepo, and htmlUrl come from
     * GitHub API data, which is richer than what the entity stores.
     *
     * <p>{@code id} and {@code webhookEnabled} come from the persisted entity (null / false
     * for repositories the user has not yet selected). This ensures the frontend:
     * <ul>
     *   <li>Knows the internal DB id needed to call {@code POST /repos/{id}/enable}.</li>
     *   <li>Shows the correct monitoring state on initial page load.</li>
     * </ul>
     *
     * @param dto            the GitHub API repo object
     * @param selected       whether this repo is already being tracked by this user
     * @param id             the internal DB primary key; null if not yet selected
     * @param webhookEnabled true if a webhook is currently registered; false otherwise
     */
    public static RepositoryResponse from(GitHubRepoDto dto, boolean selected,
                                          Long id, boolean webhookEnabled) {
        return new RepositoryResponse(
                id,
                dto.getId(),
                dto.getOwnerLogin(),
                dto.getName(),
                dto.getFullName(),
                dto.getDescription(),
                dto.isPrivateRepo(),
                dto.getHtmlUrl(),
                selected,
                webhookEnabled,
                null  // webhookId not exposed in list — use from(Repository) for full state
        );
    }

    /**
     * Creates a response from a persisted {@link Repository} entity.
     * Used by:
     * <ul>
     *   <li>{@code POST /repos/select} — repo is selected, webhook not yet enabled.</li>
     *   <li>{@code POST /repos/{id}/enable} — webhook just enabled; {@code webhookId} is set.</li>
     *   <li>{@code POST /repos/{id}/disable} — webhook just disabled; {@code webhookId} is null.</li>
     * </ul>
     * Returns full entity state so the frontend can update itself without a follow-up GET.
     *
     * @param repo the persisted repository entity
     */
    public static RepositoryResponse from(Repository repo) {
        return new RepositoryResponse(
                repo.getId(),                // internal DB id — used for enable/disable calls
                repo.getGithubRepoId(),
                repo.getOwner(),
                repo.getName(),
                repo.getFullName(),
                null,                        // description not stored on entity
                false,                       // privateRepo not stored on entity
                null,                        // htmlUrl not stored on entity
                true,                        // always selected when returned from entity
                repo.getWebhookEnabled(),
                repo.getWebhookId()
        );
    }

    // ---- Getters (Jackson serializes via getters) ----

    public Long    getId()             { return id; }
    public Long    getGithubRepoId()   { return githubRepoId; }
    public String  getOwner()          { return owner; }
    public String  getName()           { return name; }
    public String  getFullName()       { return fullName; }
    public String  getDescription()    { return description; }
    public boolean isPrivateRepo()     { return privateRepo; }
    public String  getHtmlUrl()        { return htmlUrl; }
    public boolean isSelected()        { return selected; }
    public boolean isWebhookEnabled()  { return webhookEnabled; }
    public Long    getWebhookId()      { return webhookId; }
}
