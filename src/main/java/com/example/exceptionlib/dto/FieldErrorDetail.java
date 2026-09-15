package com.example.exceptionlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** One field-level validation failure, as reported by Bean Validation or a {@link com.example.exceptionlib.exception.ValidationException}. */
@Getter
@Builder
@AllArgsConstructor
public class FieldErrorDetail {
    private final String field;
    private final String message;
    private final Object rejectedValue;
}
