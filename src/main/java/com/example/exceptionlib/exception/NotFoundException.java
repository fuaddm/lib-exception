package com.example.exceptionlib.exception;

import com.example.exceptionlib.error.CommonErrorCode;
import com.example.exceptionlib.error.ErrorCode;

/** Thrown when a requested resource does not exist. Maps to HTTP 404. */
public class NotFoundException extends BaseException {

    public NotFoundException(String message) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
