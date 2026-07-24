package com.prreviewer.auth;

import com.prreviewer.config.AppProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Invoked by Spring Security immediately after a successful GitHub OAuth2 login.
 *
 * <p>Responsibilities (in order):
 * <ol>
 *   <li>Extract user attributes from the GitHub OAuth2 response.</li>
 *   <li>Call {@link UserService#upsertUser} to persist/update the user record.</li>
 *   <li>Redirect the browser to the frontend URL so the React SPA takes control.</li>
 * </ol>
 *
 * <p>This handler is intentionally thin — it extracts, delegates, and redirects.
 * All business logic lives in {@link UserService}.
 *
 * <p><strong>GitHub OAuth2 attribute mapping:</strong>
 * <ul>
 *   <li>{@code id} — the GitHub numeric user ID (our permanent identity key).
 *       Returned as {@code Integer} or {@code Long} depending on the value size.</li>
 *   <li>{@code login} — the GitHub username/handle.</li>
 *   <li>{@code email} — the primary email; may be {@code null} if the user has
 *       set their email to private on GitHub.</li>
 * </ul>
 *
 * <p><strong>Access token extraction:</strong>
 * Spring's {@link OAuth2AuthenticationToken} wraps the underlying
 * {@link org.springframework.security.oauth2.client.OAuth2AuthorizedClient}, but
 * the token is not directly on the token object. We obtain it by casting to
 * {@link OAuth2AuthenticationToken} and reading from the authorized client via
 * the {@link org.springframework.security.oauth2.client.OAuth2AuthorizedClientService}.
 * To avoid pulling in that service here, we store the token in the OAuth2User
 * attributes where Spring already placed it — accessible via
 * {@code principal.getAttribute("access_token")} after configuring the
 * token response client, OR we inject
 * {@link org.springframework.security.oauth2.client.OAuth2AuthorizedClientService}
 * directly. We use the latter: it is the correct, non-hacky approach.
 */
@Component
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthSuccessHandler.class);

    private final UserService userService;
    private final AppProperties appProperties;
    private final org.springframework.security.oauth2.client.OAuth2AuthorizedClientService authorizedClientService;

    public AuthSuccessHandler(
            UserService userService,
            AppProperties appProperties,
            org.springframework.security.oauth2.client.OAuth2AuthorizedClientService authorizedClientService) {
        this.userService             = userService;
        this.appProperties           = appProperties;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = oauthToken.getPrincipal();

        // ---- Extract GitHub user attributes ----------------------------------------

        // GitHub returns the numeric user ID as an Integer in the attributes map.
        // We store it as a String (our permanent identity key) to avoid type ambiguity.
        Object rawId   = principal.getAttribute("id");
        String githubId = rawId != null ? String.valueOf(rawId) : null;

        String username = principal.getAttribute("login");
        String email    = principal.getAttribute("email"); // nullable — can be private on GitHub

        // ---- Extract the OAuth2 access token ---------------------------------------
        // We retrieve the authorized client for this user+registration combination,
        // then read the access token value from it.
        var authorizedClient = authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(),
            oauthToken.getName()
        );

        String accessToken = (authorizedClient != null)
            ? authorizedClient.getAccessToken().getTokenValue()
            : null;

        // ---- Validate extracted data ------------------------------------------------
        if (githubId == null || githubId.isBlank()) {
            log.error("OAuth success handler: GitHub ID is missing from attributes. Aborting login.");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "GitHub ID missing from OAuth response");
            return;
        }

        if (accessToken == null) {
            log.error("OAuth success handler: access token is null for githubId={}. Aborting login.", githubId);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Access token missing from OAuth response");
            return;
        }

        // ---- Persist the user ------------------------------------------------------
        log.info("OAuth login success: githubId={}, username={}", githubId, username);
        userService.upsertUser(githubId, username, email, accessToken);

        // ---- Redirect to frontend --------------------------------------------------
        // The React SPA handles the post-login routing (e.g. to /dashboard).
        // We always redirect to the root of the frontend — the SPA decides where to go.
        String frontendUrl = appProperties.getFrontendUrl();
        log.debug("Redirecting to frontend after login: {}", frontendUrl);

        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
