package com.prreviewer.exception;

/**
 * Thrown when a webhook delivery ID has already been processed.
 * Used to enforce idempotency — GitHub may resend deliveries.
 */
public class DuplicateWebhookDeliveryException extends RuntimeException {
    public DuplicateWebhookDeliveryException(String deliveryId) {
        super("Webhook delivery already processed: " + deliveryId);
    }
}
