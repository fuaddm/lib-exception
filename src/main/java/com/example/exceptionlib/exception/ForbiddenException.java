package com.example.exceptionlib.exception;

import com.example.exceptionlib.error.CommonErrorCode;
import com.example.exceptionlib.error.ErrorCode;

/** Thrown when the caller is authenticated but not allowed to perform the action. Maps to HTTP 403. */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(CommonErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
