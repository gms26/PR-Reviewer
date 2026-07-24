package com.prreviewer.auth;

import com.prreviewer.exception.ResourceNotFoundException;
import com.prreviewer.model.User;
import com.prreviewer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Converts a Spring Security {@link OAuth2User} principal into the application's
 * {@link User} entity by looking up the GitHub ID in the database.
 *
 * <h2>Why this exists</h2>
 * <p>Every controller that needs the authenticated user must perform the same
 * lookup: extract the numeric GitHub ID from the OAuth2 principal, then find the
 * corresponding {@link User} entity. Without this service, each controller
 * duplicates a private {@code resolveUser()} method — a violation of DRY that
 * silently diverges over time.
 *
 * <h2>Why we load from the database</h2>
 * <p>The OAuth2 session principal carries attributes as they were at login time.
 * By re-loading from the database we always get the current {@code accessToken},
 * {@code email}, and {@code lastLoginAt} — never stale session data.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * User user = currentUserService.resolve(principal);
 * }</pre>
 *
 * <h2>Note on AuthController</h2>
 * <p>{@link com.prreviewer.auth.AuthController} predates this service and uses
 * an inline {@code resolveUser()} method. It has been approved and is not modified
 * here. New controllers should inject and use {@code CurrentUserService} directly.
 */
@Service
public class CurrentUserService {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolves an authenticated GitHub OAuth2 principal to a {@link User} entity.
     *
     * <p>Extracts the numeric GitHub user ID from the principal's {@code "id"}
     * attribute, then queries the database. The principal is injected by Spring
     * Security via {@code @AuthenticationPrincipal}; if there is no active
     * session, Spring returns {@code 401} before this method is ever called.
     *
     * @param principal the authenticated GitHub OAuth2 user
     * @return the corresponding database {@link User} entity with a current
     *         {@code accessToken}
     * @throws ResourceNotFoundException if the authenticated user is not in the
     *         database (indicates session / DB inconsistency)
     */
    public User resolve(OAuth2User principal) {
        Object rawId    = principal.getAttribute("id");
        String githubId = rawId != null ? String.valueOf(rawId) : null;

        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in DB: githubId={}", githubId);
                    return new ResourceNotFoundException("User", githubId);
                });
    }
}
