package com.prreviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Entry point for the PR Reviewer application.
 *
 * <p>@EnableRetry activates Spring Retry support, used for transient failure
 * retries against GitHub and Gemini APIs.
 */
@SpringBootApplication
@EnableRetry
public class PrReviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrReviewerApplication.class, args);
    }
}
