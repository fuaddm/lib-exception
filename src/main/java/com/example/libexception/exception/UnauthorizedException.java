package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

/**
 * Thrown when authentication is missing or invalid (e.g. bad/expired token). Maps to HTTP 401.
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(CommonErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(CommonErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
