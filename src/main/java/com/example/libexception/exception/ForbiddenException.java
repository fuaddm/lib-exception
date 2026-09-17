package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

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
