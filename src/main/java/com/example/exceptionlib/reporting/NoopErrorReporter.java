package com.example.exceptionlib.reporting;

/** Used when no error-tracking DSN is configured, so {@code GlobalExceptionHandler} always has a reporter to call. */
public class NoopErrorReporter implements ErrorReporter {

    @Override
    public void report(Throwable throwable) {
        // no DSN configured - reporting is intentionally a no-op
    }
}
