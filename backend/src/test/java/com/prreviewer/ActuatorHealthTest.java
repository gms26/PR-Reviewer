package com.prreviewer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
    "server.port=8080",
    "management.server.port=8080",
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "GITHUB_CLIENT_ID=mock",
    "GITHUB_CLIENT_SECRET=mock",
    "GITHUB_WEBHOOK_SECRET=mock",
    "GEMINI_API_KEY=mock",
    "DATABASE_URL=jdbc:h2:mem:testdb;MODE=PostgreSQL",
    "DATABASE_USERNAME=mock"
})
public class ActuatorHealthTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHealth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        System.out.println("=================================================");
        System.out.println("RESPONSE STATUS: " + response.getStatusCode());
        System.out.println("RESPONSE BODY: " + response.getBody());
        System.out.println("=================================================");
    }
}
