package com.prreviewer.exception;

/**
 * Thrown when a Gemini API call fails with a non-transient error.
 * Transient failures are handled by Spring Retry before this is thrown.
 */
public class GeminiApiException extends RuntimeException {
    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
