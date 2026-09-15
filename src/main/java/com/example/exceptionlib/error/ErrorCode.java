package com.example.exceptionlib.error;

import org.springframework.http.HttpStatus;

/**
 * Contract for a domain/service specific error code.
 * <p>
 * Each microservice should define its own enum implementing this interface
 * (e.g. {@code PinErrorCode}, {@code UserErrorCode}) so error codes stay scoped
 * to the service that owns them, while still plugging into the shared
 * exception hierarchy and {@link com.example.exceptionlib.handler.GlobalExceptionHandler}.
 *
 * <pre>{@code
 * public enum PinErrorCode implements ErrorCode {
 *     PIN_NOT_FOUND(HttpStatus.NOT_FOUND, "Pin could not be found.");
 *
 *     private final HttpStatus httpStatus;
 *     private final String defaultMessage;
 *
 *     PinErrorCode(HttpStatus httpStatus, String defaultMessage) {
 *         this.httpStatus = httpStatus;
 *         this.defaultMessage = defaultMessage;
 *     }
 *
 *     public String getCode() { return name(); }
 *     public HttpStatus getHttpStatus() { return httpStatus; }
 *     public String getDefaultMessage() { return defaultMessage; }
 * }
 * }</pre>
 */
public interface ErrorCode {

    /**
     * Stable, machine-readable identifier returned to API clients, e.g. {@code "PIN_NOT_FOUND"}.
     * Treat it as part of your public API contract - do not change it once shipped.
     */
    String getCode();

    /**
     * HTTP status this error maps to when serialized by the global exception handler.
     */
    HttpStatus getHttpStatus();

    /**
     * Default, human-readable message. Can be overridden per-throw-site.
     */
    String getDefaultMessage();
}
