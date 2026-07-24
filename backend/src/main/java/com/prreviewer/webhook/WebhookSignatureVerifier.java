package com.prreviewer.webhook;

import com.prreviewer.config.AppProperties;
import com.prreviewer.exception.WebhookSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Validates the HMAC-SHA256 signature sent by GitHub with webhook payloads.
 *
 * <p>GitHub signs the raw HTTP body using the configured webhook secret.
 * This class recomputes the signature locally and verifies it matches the
 * {@code X-Hub-Signature-256} header. This serves as the sole authentication
 * mechanism for incoming webhooks.
 */
@Component
public class WebhookSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(WebhookSignatureVerifier.class);
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "sha256=";

    private final AppProperties appProperties;

    public WebhookSignatureVerifier(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Verifies the signature against the raw payload.
     *
     * @param rawPayload the exact, unmodified HTTP request body
     * @param signatureHeader the value of the X-Hub-Signature-256 header (must start with "sha256=")
     * @throws WebhookSignatureException if the signature is missing, malformed, or invalid
     */
    public void verify(String rawPayload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new WebhookSignatureException("Missing X-Hub-Signature-256 header");
        }

        if (!signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            throw new WebhookSignatureException("Invalid signature prefix. Expected 'sha256='");
        }

        String expectedSignature = computeSignature(rawPayload);
        String providedSignature = signatureHeader.substring(SIGNATURE_PREFIX.length());

        // Use constant-time comparison to prevent timing attacks
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new WebhookSignatureException("Signature mismatch");
        }

        log.debug("Webhook signature verified successfully");
    }

    private String computeSignature(String payload) {
        try {
            String secret = appProperties.getGithubWebhookSecret();
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("GitHub webhook secret is not configured");
            }

            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to compute HMAC signature: {}", e.getMessage(), e);
            throw new WebhookSignatureException("Internal error verifying signature");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
