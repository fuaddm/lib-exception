package com.example.libexception.handler;

import com.example.libexception.dto.ErrorResponse;
import com.example.libexception.dto.FieldErrorDetail;
import com.example.libexception.error.CommonErrorCode;
import com.example.libexception.exception.*;
import com.example.libexception.reporting.ErrorReporter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Central translation point from exceptions to the shared {@link ErrorResponse} shape.
 * <p>
 * Auto-registered into every consuming Spring Boot service via
 * {@code com.example.exceptionlib.autoconfigure.ExceptionLibAutoConfiguration} -
 * no manual component scanning of this library's package is required.
 * <p>
 * Runs at {@link Ordered#LOWEST_PRECEDENCE} so a service can define its own
 * {@code @RestControllerAdvice} for cases it wants to handle differently; that
 * advice takes precedence over this default one.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_MDC_KEY = "traceId";

    private final ErrorReporter errorReporter;
    private final int minimumReportableStatus;

    public GlobalExceptionHandler(ErrorReporter errorReporter, int minimumReportableStatus) {
        this.errorReporter = errorReporter;
        this.minimumReportableStatus = minimumReportableStatus;
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode().getHttpStatus();
        logByStatus(status, ex, request);

        List<FieldErrorDetail> fieldErrors = ex instanceof ValidationException ve ? ve.getFieldErrors() : null;

        return build(status, ex.getErrorCode().getCode(), ex.getMessage(), request, fieldErrors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .toList();

        log.warn("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, CommonErrorCode.VALIDATION_FAILED.getCode(),
                CommonErrorCode.VALIDATION_FAILED.getDefaultMessage(), request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toFieldErrorDetail)
                .toList();

        log.warn("Constraint violation on {}: {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, CommonErrorCode.VALIDATION_FAILED.getCode(),
                CommonErrorCode.VALIDATION_FAILED.getDefaultMessage(), request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request body on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, CommonErrorCode.BAD_REQUEST.getCode(),
                "Malformed JSON request body.", request, null);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestParams(Exception ex, HttpServletRequest request) {
        log.warn("Bad request parameter on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, CommonErrorCode.BAD_REQUEST.getCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        errorReporter.report(ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                CommonErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), request, null);
    }

    @ExceptionHandler(WebApplicationException.class)
    public ResponseEntity<ErrorResponse> handleWebApplicationException(WebApplicationException ex, HttpServletRequest request) {
        int status = ex.getResponse().getStatus();

        BaseException mappedException = switch (status) {
            case 400 -> new BadRequestException();
            case 401 -> new UnauthorizedException();
            case 403 -> new ForbiddenException();
            case 404 -> new NotFoundException();
            case 409 -> new ConflictException();
            case 429 -> new TooManyRequestsException();
            default -> new InternalServerErrorException();
        };

        return handleBaseException(mappedException, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest request, List<FieldErrorDetail> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .traceId(currentTraceId())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String currentTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    private void logByStatus(HttpStatus status, BaseException ex, HttpServletRequest request) {
        if (status.is5xxServerError()) {
            log.error("{} on {}: {}", ex.getErrorCode().getCode(), request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("{} on {}: {}", ex.getErrorCode().getCode(), request.getRequestURI(), ex.getMessage());
        }
        if (status.value() >= minimumReportableStatus) {
            errorReporter.report(ex);
        }
    }

    private FieldErrorDetail toFieldErrorDetail(FieldError fe) {
        return FieldErrorDetail.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .rejectedValue(fe.getRejectedValue())
                .build();
    }

    private FieldErrorDetail toFieldErrorDetail(ConstraintViolation<?> cv) {
        return FieldErrorDetail.builder()
                .field(cv.getPropertyPath().toString())
                .message(cv.getMessage())
                .rejectedValue(cv.getInvalidValue())
                .build();
    }
}
