package com.example.libexception.error;

import com.example.libexception.handler.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;

/**
 * Contract for a domain/service specific error code.
 * <p>
 * Each microservice should define its own enum implementing this interface
 * (e.g. {@code PinErrorCode}, {@code UserErrorCode}) so error codes stay scoped
 * to the service that owns them, while still plugging into the shared
 * exception hierarchy and {@link GlobalExceptionHandler}.
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

    String getCode();
    HttpStatus getHttpStatus();
    String getDefaultMessage();
}
