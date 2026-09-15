package com.example.exceptionlib.handler;

import com.example.exceptionlib.dto.ErrorResponse;
import com.example.exceptionlib.error.CommonErrorCode;
import com.example.exceptionlib.exception.ConflictException;
import com.example.exceptionlib.exception.NotFoundException;
import com.example.exceptionlib.reporting.ErrorReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final List<Throwable> reported = new ArrayList<>();
    private final ErrorReporter recordingReporter = reported::add;

    // minimumReportableStatus = 500: only server errors get forwarded to the error tracker.
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(recordingReporter, 500);

    @BeforeEach
    void resetRecordedReports() {
        reported.clear();
    }

    @Test
    void mapsNotFoundExceptionTo404WithMatchingCodeAndDoesNotReport() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/pins/42");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(
                new NotFoundException("Pin 42 not found"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND.getCode(), body.getCode());
        assertEquals("Pin 42 not found", body.getMessage());
        assertEquals("/api/pins/42", body.getPath());
        assertNotNull(body.getTimestamp());
        assertTrue(reported.isEmpty(), "4xx errors should not be forwarded to the error tracker by default");
    }

    @Test
    void mapsConflictExceptionTo409AndDoesNotReport() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");

        ResponseEntity<ErrorResponse> response = handler.handleBaseException(
                new ConflictException("Username already taken"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(CommonErrorCode.CONFLICT.getCode(), response.getBody().getCode());
        assertTrue(reported.isEmpty());
    }

    @Test
    void unexpectedExceptionMapsTo500AndHidesInternalMessageAndReports() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/boards");
        RuntimeException ex = new RuntimeException("npe at line 42, connection pool exhausted");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(), body.getCode());
        assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), body.getMessage());
        assertNull(body.getErrors());
        assertEquals(List.of(ex), reported, "unexpected (500) exceptions should be forwarded to the error tracker");
    }
}
