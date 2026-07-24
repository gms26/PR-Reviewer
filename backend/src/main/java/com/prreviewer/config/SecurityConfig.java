package com.prreviewer.config;

import com.prreviewer.auth.AuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration.
 *
 * <p>Key decisions:
 * <ul>
 *   <li><strong>CSRF disabled:</strong> This is a REST API consumed by a React SPA.
 *       State-mutating calls use Axios with {@code withCredentials: true} and a
 *       session cookie, not HTML form submissions. The webhook endpoint has its own
 *       HMAC-SHA256 signature verification which provides equivalent protection.</li>
 *   <li><strong>/webhook/github permitted:</strong> GitHub cannot carry a session
 *       cookie. Signature verification in the handler acts as authentication.</li>
 *   <li><strong>401 instead of redirect:</strong> Unauthenticated API requests return
 *       {@code 401 Unauthorized} rather than redirecting to a login page. The React
 *       SPA handles the auth state and performs the redirect to /login itself.</li>
 *   <li><strong>Custom success handler:</strong> {@link AuthSuccessHandler} persists
 *       the authenticated user to the database and redirects to the frontend URL.
 *       It replaces Spring's default success handler which would redirect to a
 *       backend URL and not persist the user.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final AuthSuccessHandler authSuccessHandler;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
                          AuthSuccessHandler authSuccessHandler) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.authSuccessHandler      = authSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public: infrastructure probes — no auth required
                .requestMatchers(
                    "/health",
                    "/actuator/health"
                ).permitAll()
                // Public: GitHub cannot carry a session cookie; HMAC signature
                // verification in the handler acts as the authentication mechanism.
                .requestMatchers("/webhook/github").permitAll()
                // Public: Spring OAuth2 redirect URIs — must be reachable before
                // any session exists, and after GitHub redirects back.
                .requestMatchers(
                    "/login/oauth2/**",
                    "/oauth2/**",
                    "/auth/success",
                    "/auth/failure"
                ).permitAll()
                // Everything else — authenticated session required.
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                // Wire in our custom success handler so every successful OAuth
                // login persists the user and redirects to the frontend.
                .successHandler(authSuccessHandler)
                .failureUrl("/auth/failure")
            )
            .exceptionHandling(ex -> ex
                // Return 401 for unauthenticated API calls instead of redirecting to login page.
                // The React SPA listens for 401 in its Axios interceptor and redirects to /login.
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) ->
                    response.setStatus(HttpStatus.OK.value()))
            );

        return http.build();
    }
}
