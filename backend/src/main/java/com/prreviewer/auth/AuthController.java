package com.prreviewer.auth;

import com.prreviewer.dto.UserResponse;
import com.prreviewer.exception.ResourceNotFoundException;
import com.prreviewer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for authentication state.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /auth/me} — returns the authenticated user's profile.</li>
 * </ul>
 *
 * <p><strong>Logout:</strong> {@code POST /auth/logout} is handled entirely by
 * Spring Security's logout filter (configured in {@link com.prreviewer.config.SecurityConfig}).
 * The filter intercepts the request before it reaches any controller, clears the
 * session and cookie, then returns {@code 200 OK} via the custom
 * {@code logoutSuccessHandler}. No controller method is needed here.
 *
 * <p><strong>Auth failure redirect:</strong> {@code GET /auth/failure} returns a
 * plain error response. It is reached when GitHub denies authorization or an
 * OAuth error occurs.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the current authenticated user's profile.
     *
     * <p>Spring Security injects the {@link OAuth2User} from the active session
     * via {@code @AuthenticationPrincipal}. If there is no active session,
     * Spring's {@link org.springframework.security.web.authentication.HttpStatusEntryPoint}
     * intercepts the request and returns {@code 401} before this method is invoked.
     *
     * <p>We use {@code githubId} from the OAuth2 principal to look up the user in
     * the database rather than returning raw OAuth2 attributes — this ensures the
     * response always reflects the current DB state (e.g. updated email, lastLoginAt).
     *
     * @param principal the authenticated GitHub OAuth2 user (injected by Spring Security)
     * @return {@code 200 OK} with {@link UserResponse}, or {@code 401} if not authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.noContent().build();
        }

        Object rawId    = principal.getAttribute("id");
        String githubId = rawId != null ? String.valueOf(rawId) : null;

        log.debug("GET /auth/me: githubId={}", githubId);

        var user = userRepository.findByGithubId(githubId)
            .orElseThrow(() -> {
                log.error("Authenticated user not found in DB: githubId={}", githubId);
                return new ResourceNotFoundException("User", githubId);
            });

        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * OAuth failure endpoint.
     *
     * <p>GitHub redirects here when the user denies authorization or an
     * OAuth error occurs. Returns {@code 401 Unauthorized} with a human-readable
     * message. The React frontend checks for this response and shows an error page.
     */
    @GetMapping("/failure")
    public ResponseEntity<String> failure() {
        log.warn("GET /auth/failure: GitHub OAuth authorization failed or was denied.");
        return ResponseEntity.status(401).body("GitHub authorization failed. Please try again.");
    }

    /**
     * OAuth success redirect target.
     *
     * <p>In normal operation, {@link AuthSuccessHandler} redirects the browser
     * directly to the frontend URL — this endpoint is never reached.
     * It exists as a safety net in case the redirect fails or is misconfigured.
     */
    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("Login successful.");
    }
}
