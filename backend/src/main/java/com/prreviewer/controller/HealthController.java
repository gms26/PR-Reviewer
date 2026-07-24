package com.prreviewer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint.
 *
 * <p>Used by Render, load balancers, and infrastructure probes to verify
 * the service is alive. Returns a structured JSON body with enough
 * information for humans and tools to confirm startup succeeded.
 *
 * <p>This is distinct from the Spring Actuator {@code /actuator/health}
 * endpoint — it is a lightweight custom endpoint with no Actuator dependency
 * for clients that prefer a simple contract.
 */
@RestController
public class HealthController {

    @Value("${spring.application.name:pr-reviewer}")
    private String applicationName;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        // Use LinkedHashMap to guarantee field order in JSON output
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",      "UP");
        body.put("application", applicationName);
        body.put("version",     "1.0");
        body.put("timestamp",   Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
