package com.example.exceptionlib.exception;

import com.example.exceptionlib.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BaseExceptionTest {

    @Test
    void usesDefaultMessageWhenNoneGiven() {
        NotFoundException ex = new NotFoundException(CommonErrorCode.RESOURCE_NOT_FOUND);

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage(), ex.getMessage());
        assertSame(CommonErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void customMessageOverridesDefault() {
        NotFoundException ex = new NotFoundException("Pin 42 not found");

        assertEquals("Pin 42 not found", ex.getMessage());
        assertSame(CommonErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void conflictExceptionMapsToConflictErrorCode() {
        ConflictException ex = new ConflictException("Username already taken");

        assertSame(CommonErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals(CommonErrorCode.CONFLICT.getHttpStatus(), ex.getErrorCode().getHttpStatus());
    }
}
