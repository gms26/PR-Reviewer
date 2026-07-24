package com.prreviewer.auth;

import com.prreviewer.model.User;
import com.prreviewer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Business logic for GitHub-authenticated user lifecycle.
 *
 * <p>Single responsibility: given data from a successful GitHub OAuth response,
 * either create a new user record or update an existing one. This operation is
 * called exactly once per successful login, from {@link AuthSuccessHandler}.
 *
 * <p><strong>Upsert contract:</strong>
 * <ul>
 *   <li>Find by {@code githubId} — the permanent GitHub numeric user ID.</li>
 *   <li>If not found: INSERT a new {@link User} record.</li>
 *   <li>If found: UPDATE {@code username}, {@code email}, {@code accessToken},
 *       and {@code lastLoginAt}. Never touch {@code id} or {@code createdAt}.</li>
 * </ul>
 *
 * <p>{@code createdAt} immutability is enforced at two levels:
 * <ol>
 *   <li>This service never calls a setter for {@code createdAt}.</li>
 *   <li>The JPA column definition has {@code updatable = false}, so Hibernate
 *       physically cannot include it in an UPDATE statement.</li>
 * </ol>
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Upserts a user from a successful GitHub OAuth login.
     *
     * <p>This method is transactional so that the find and the subsequent save
     * are atomic — no concurrent login from the same GitHub account can create
     * a duplicate user record.
     *
     * @param githubId    the GitHub numeric user ID (permanent, never changes)
     * @param username    the GitHub login/username (can change on GitHub)
     * @param email       the primary email from GitHub (can be null if private)
     * @param accessToken the OAuth access token issued by GitHub for this session
     * @return the persisted {@link User} entity (either newly created or updated)
     */
    @Transactional
    public User upsertUser(String githubId, String username, String email, String accessToken) {
        return userRepository.findByGithubId(githubId)
            .map(existingUser -> updateExistingUser(existingUser, username, email, accessToken))
            .orElseGet(() -> createNewUser(githubId, username, email, accessToken));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private User updateExistingUser(User user, String username, String email, String accessToken) {
        log.debug("Updating existing user: githubId={}, username={}", user.getGithubId(), username);

        user.setUsername(username);
        user.setEmail(email);
        user.setAccessToken(accessToken);
        user.setLastLoginAt(Instant.now());

        // userRepository.save() is not strictly required here because the entity
        // is already managed (loaded within the same transaction), but we call it
        // explicitly for clarity and to guarantee the return value is the
        // fully-persisted, ID-bearing entity.
        return userRepository.save(user);
    }

    private User createNewUser(String githubId, String username, String email, String accessToken) {
        log.info("Creating new user: githubId={}, username={}", githubId, username);

        User newUser = User.builder()
            .githubId(githubId)
            .username(username)
            .email(email)
            .accessToken(accessToken)
            .build();

        // Set lastLoginAt on first login — it should never be null after login
        newUser.setLastLoginAt(Instant.now());

        return userRepository.save(newUser);
    }
}
