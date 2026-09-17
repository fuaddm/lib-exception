package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

/** Thrown when a request conflicts with the current state of a resource (e.g. duplicate username). Maps to HTTP 409. */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(CommonErrorCode.CONFLICT, message);
    }

    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
