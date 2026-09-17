package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

/** Thrown when a client has exceeded a rate limit. Maps to HTTP 429. */
public class TooManyRequestsException extends BaseException {

    public TooManyRequestsException(String message) {
        super(CommonErrorCode.TOO_MANY_REQUESTS, message);
    }

    public TooManyRequestsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TooManyRequestsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
