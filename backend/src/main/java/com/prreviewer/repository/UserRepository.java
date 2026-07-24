package com.prreviewer.repository;

import com.prreviewer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGithubId(String githubId);

    boolean existsByGithubId(String githubId);
}
