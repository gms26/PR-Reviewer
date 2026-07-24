package com.prreviewer.repository;

import com.prreviewer.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JPA repository for {@link Repository} entities.
 */
@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    List<Repository> findByUserId(Long userId);

    Optional<Repository> findByGithubRepoId(Long githubRepoId);

    Optional<Repository> findByOwnerAndName(String owner, String name);

    boolean existsByGithubRepoId(Long githubRepoId);

    /**
     * Used by {@code RepositoryService.selectRepository} to check idempotency:
     * returns the existing entity if this user already has this repo persisted,
     * preventing duplicate rows on repeated "Monitor" clicks.
     *
     * @param githubRepoId the GitHub repository ID
     * @param userId       the internal user ID (not githubId)
     */
    Optional<Repository> findByGithubRepoIdAndUserId(Long githubRepoId, Long userId);

    /**
     * Returns all persisted GitHub repo IDs for a given user.
     * Used by {@code RepositoryService.listRepositories} to efficiently
     * mark which repos in the GitHub-returned list are already selected,
     * without fetching full entity objects.
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT r.githubRepoId FROM Repository r WHERE r.user.id = :userId")
    Set<Long> findGithubRepoIdsByUserId(Long userId);
}
