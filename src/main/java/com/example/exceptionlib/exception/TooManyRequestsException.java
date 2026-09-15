package com.example.exceptionlib.exception;

import com.example.exceptionlib.error.CommonErrorCode;
import com.example.exceptionlib.error.ErrorCode;

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
