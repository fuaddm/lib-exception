package com.example.libexception.exception;

import com.example.libexception.dto.ErrorResponse;
import com.example.libexception.error.ErrorCode;
import com.example.libexception.handler.GlobalExceptionHandler;
import lombok.Getter;

/**
 * Root of the shared exception hierarchy. Carries an {@link ErrorCode} so
 * {@link GlobalExceptionHandler} can translate any
 * subclass into a consistent {@link ErrorResponse}
 * without each microservice writing its own translation logic.
 * <p>
 * Prefer one of the concrete subclasses ({@link NotFoundException}, {@link ConflictException}, ...)
 * over extending this directly, unless you need a status code that none of them cover.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
