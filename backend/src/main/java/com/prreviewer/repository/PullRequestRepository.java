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
}
