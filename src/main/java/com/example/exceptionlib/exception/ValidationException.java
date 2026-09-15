package com.example.exceptionlib.exception;

import com.example.exceptionlib.dto.FieldErrorDetail;
import com.example.exceptionlib.error.CommonErrorCode;
import lombok.Getter;

import java.util.List;

/**
 * Thrown for application-level validation failures that happen outside of
 * Bean Validation (e.g. cross-field business rules checked in a service layer).
 * Bean Validation failures on {@code @Valid} controller arguments are already
 * handled directly by {@link com.example.exceptionlib.handler.GlobalExceptionHandler}
 * and don't need this exception.
 */
@Getter
public class ValidationException extends BaseException {

    private final List<FieldErrorDetail> fieldErrors;

    public ValidationException(String message, List<FieldErrorDetail> fieldErrors) {
        super(CommonErrorCode.VALIDATION_FAILED, message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(List<FieldErrorDetail> fieldErrors) {
        this(CommonErrorCode.VALIDATION_FAILED.getDefaultMessage(), fieldErrors);
    }
}
