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
    "spring.datasource.url=jdbc:h2:mem:pr_reviewer_test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=sa",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
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
