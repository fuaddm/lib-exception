package com.example.exceptionlib.reporting;

/**
 * Forwards an exception caught by {@link com.example.exceptionlib.handler.GlobalExceptionHandler}
 * to an external error-tracking system. Kept as an interface so the default
 * Sentry-protocol implementation can be swapped (or no-op'd) without touching the handler.
 */
public interface ErrorReporter {

    void report(Throwable throwable);
}
