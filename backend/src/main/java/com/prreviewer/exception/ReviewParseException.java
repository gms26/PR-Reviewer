package com.prreviewer.exception;

/**
 * Thrown when the AI's response cannot be parsed into the expected
 * domain models (e.g. invalid JSON, missing required fields, or
 * invalid enum values).
 */
public class ReviewParseException extends RuntimeException {
    public ReviewParseException(String message) {
        super(message);
    }

    public ReviewParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
