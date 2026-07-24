package com.prreviewer.gemini;

import com.prreviewer.config.AppProperties;
import com.prreviewer.exception.GeminiApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Infrastructure service responsible solely for executing HTTP calls to the
 * Google Gemini REST API.
 *
 * <p>Contains zero business logic and performs no parsing of the AI's response
 * beyond extracting the raw text string from the Gemini JSON envelope.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final RestClient restClient;
    private final AppProperties appProperties;

    public GeminiService(@Qualifier("geminiRestClient") RestClient restClient, AppProperties appProperties) {
        this.restClient = restClient;
        this.appProperties = appProperties;
    }

    /**
     * Executes the HTTP call to Gemini.
     * Retries on 429 Too Many Requests and 5xx Server Errors.
     *
     * @param prompt the complete prompt string
     * @return the raw text response from the model
     * @throws GeminiApiException if the network call fails permanently or returns a 4xx error
     */
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class, RestClientException.class},
            noRetryFor = {HttpClientErrorException.class}, // 4xx errors other than 429 will not be retried
            maxAttemptsExpression = "${app.gemini.max-retries}",
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String generateReview(String prompt) {
        log.info("Sending review prompt to Gemini API...");
        long start = System.currentTimeMillis();

        String url = appProperties.getGeminiApiUrl() + "?key=" + appProperties.getGeminiApiKey();

        GeminiRequest request = new GeminiRequest(List.of(
                new Content(List.of(new Part(prompt)))
        ));

        try {
            GeminiResponse response = restClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new GeminiApiException("Gemini API returned an empty or malformed response envelope.");
            }

            String text = response.candidates().get(0).content().parts().get(0).text();
            
            log.info("Gemini API call completed in {}ms", System.currentTimeMillis() - start);
            return text;

        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("Gemini API rate limit exceeded (HTTP 429). Retrying...");
            throw ex; // Let @Retryable handle it
        } catch (HttpClientErrorException ex) {
            log.error("Gemini API returned HTTP {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            // Do not retry 400, 401, 403, etc.
            throw new GeminiApiException("Gemini API HTTP Error: " + ex.getStatusCode(), ex);
        } catch (HttpServerErrorException ex) {
            log.warn("Gemini API returned server error HTTP {}. Retrying...", ex.getStatusCode());
            throw ex; // Let @Retryable handle it
        } catch (RestClientException ex) {
            log.warn("Network error calling Gemini API. Retrying...", ex);
            throw ex; // Let @Retryable handle it
        }
    }

    // --- Gemini v1beta API Request/Response Envelopes ---

    private record GeminiRequest(List<Content> contents) {}
    private record Content(List<Part> parts) {}
    private record Part(String text) {}

    private record GeminiResponse(List<Candidate> candidates) {}
    private record Candidate(Content content) {}
}
