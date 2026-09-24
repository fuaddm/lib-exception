package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404.
 */
public class NotFoundException extends BaseException {

    public NotFoundException(String message) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public NotFoundException() {
        super(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
