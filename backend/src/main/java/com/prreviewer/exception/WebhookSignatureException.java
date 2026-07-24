package com.prreviewer.exception;

/**
 * Thrown when X-Hub-Signature-256 verification fails.
 * Maps to HTTP 401 Unauthorized.
 */
public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }
}
