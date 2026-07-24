package com.prreviewer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test — verifies the Spring application context loads successfully.
 *
 * <p>Uses @TestPropertySource to override environment-dependent properties
 * with safe test defaults so the test can run without real credentials.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///pr_reviewer_test",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.security.oauth2.client.registration.github.client-id=test-id",
    "spring.security.oauth2.client.registration.github.client-secret=test-secret",
    "app.github.webhook-secret=test-secret",
    "app.gemini.api-key=test-key",
    "app.frontend.url=http://localhost:5173"
})
class PrReviewerApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context loads without error, this milestone is complete
    }
}
