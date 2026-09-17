package com.example.libexception.reporting;

import com.example.libexception.autoconfigure.LibExceptionProperties;
import io.sentry.Sentry;

/**
 * Forwards exceptions to any server speaking the Sentry event-ingestion protocol -
 * Sentry itself, self-hosted Bugsink, GlitchTip, etc. Nothing vendor-specific here;
 * behavior is entirely determined by the DSN's host.
 */
public class SentryProtocolErrorReporter implements ErrorReporter {

    public SentryProtocolErrorReporter(LibExceptionProperties.ErrorReporting config) {
        if (!Sentry.isEnabled()) {
            Sentry.init(options -> {
                options.setDsn(config.getDsn());
                if (config.getEnvironment() != null) {
                    options.setEnvironment(config.getEnvironment());
                }
                // Bugsink/GlitchTip don't implement the performance-tracing backend; keep this off.
                options.setTracesSampleRate(0.0);
            });
        }
    }

    @Override
    public void report(Throwable throwable) {
        Sentry.captureException(throwable);
    }
}
