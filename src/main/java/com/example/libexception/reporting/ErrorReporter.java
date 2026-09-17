package com.example.libexception.reporting;

import com.example.libexception.handler.GlobalExceptionHandler;

/**
 * Forwards an exception caught by {@link GlobalExceptionHandler}
 * to an external error-tracking system. Kept as an interface so the default
 * Sentry-protocol implementation can be swapped (or no-op'd) without touching the handler.
 */
public interface ErrorReporter {

    void report(Throwable throwable);
}
