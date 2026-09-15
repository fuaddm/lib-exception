package com.example.exceptionlib.exception;

import com.example.exceptionlib.error.CommonErrorCode;
import com.example.exceptionlib.error.ErrorCode;

/** Thrown when authentication is missing or invalid (e.g. bad/expired token). Maps to HTTP 401. */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(CommonErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
