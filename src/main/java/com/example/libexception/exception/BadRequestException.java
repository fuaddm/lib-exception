package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

/** Thrown when the request itself is malformed or semantically invalid. Maps to HTTP 400. */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(CommonErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
