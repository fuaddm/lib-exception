package com.example.exceptionlib.reporting;

import com.example.libexception.autoconfigure.LibExceptionProperties;
import com.example.libexception.reporting.ErrorReporter;
import com.example.libexception.reporting.SentryProtocolErrorReporter;
import io.sentry.Sentry;
import io.sentry.protocol.SentryId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Manual, opt-in connectivity check against a real Sentry-protocol server (Sentry,
 * Bugsink, GlitchTip...). Skipped by default so CI never depends on external
 * network access; run explicitly with:
 *
 * <pre>
 * BUGSINK_SMOKE_TEST=true BUGSINK_DSN="http://&lt;key&gt;@host:port/&lt;project&gt;" \
 *   ./gradlew test --tests "*SentryProtocolErrorReporterSmokeTest*"
 * </pre>
 */
@EnabledIfEnvironmentVariable(named = "BUGSINK_SMOKE_TEST", matches = "true")
class SentryProtocolErrorReporterSmokeTest {

    @AfterEach
    void shutdown() {
        Sentry.close();
    }

    @Test
    void sendsATestExceptionToTheConfiguredDsn() {
        String dsn = System.getenv("BUGSINK_DSN");

        LibExceptionProperties.ErrorReporting config = new LibExceptionProperties.ErrorReporting();
        config.setDsn(dsn);
        config.setEnvironment("exception-lib-smoke-test");

        ErrorReporter reporter = new SentryProtocolErrorReporter(config);
        Exception testException = new RuntimeException("exception-lib smoke test: hello from GlobalExceptionHandler");

        reporter.report(testException);

        SentryId eventId = Sentry.captureMessage("exception-lib smoke test flush marker");
        Sentry.flush(TimeUnit.SECONDS.toMillis(10));

        assertNotEquals(SentryId.EMPTY_ID, eventId);
        System.out.println("Sent smoke-test event. Check your Bugsink project for eventId=" + eventId);
    }
}
