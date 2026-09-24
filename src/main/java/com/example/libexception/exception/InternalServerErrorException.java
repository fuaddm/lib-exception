package com.example.libexception.exception;

import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.error.ErrorCode;

public class InternalServerErrorException extends BaseException {

    public InternalServerErrorException() {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    public InternalServerErrorException(String message) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerErrorException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}