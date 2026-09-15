package com.example.exceptionlib.error;

import org.springframework.http.HttpStatus;

/**
 * Generic, service-agnostic error codes covering the common HTTP failure cases
 * shared by every microservice. Prefer a domain-specific {@link ErrorCode} enum
 * (e.g. {@code PIN_NOT_FOUND}) when the failure is meaningful to API clients;
 * fall back to these for truly generic failures (bad input, auth, unexpected errors).
 */
public enum CommonErrorCode implements ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request could not be understood or was missing required parameters."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "One or more fields failed validation."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required or has failed."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource could not be found."),
    CONFLICT(HttpStatus.CONFLICT, "The request conflicts with the current state of the resource."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    CommonErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
