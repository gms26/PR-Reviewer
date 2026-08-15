package com.prreviewer.repository;

import com.prreviewer.model.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link PullRequest} entities.
 */
@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    Optional<PullRequest> findByRepositoryIdAndGithubPrNumber(Long repositoryId, Integer githubPrNumber);

    boolean existsByRepositoryIdAndGithubPrNumber(Long repositoryId, Integer githubPrNumber);

    @org.springframework.data.jpa.repository.Query("SELECT pr FROM PullRequest pr JOIN FETCH pr.repository r JOIN FETCH r.user WHERE pr.id = :id")
    Optional<PullRequest> findByIdWithRepositoryAndUser(@org.springframework.data.repository.query.Param("id") Long id);
}
