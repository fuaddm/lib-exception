# Error Reporting

`lib-exception` handles integrating your service's unhandled exceptions with external bug trackers.

By default, the `GlobalExceptionHandler` logs exceptions based on their HTTP status (warnings for 4xx, errors for 5xx). Additionally, if an error report destination is configured, the exception will be automatically forwarded.

## Sentry Protocol

The library includes a `SentryProtocolErrorReporter` which forwards exceptions to any server that speaks the Sentry event-ingestion protocol. This is vendor-agnostic and works with:
- [Sentry](https://sentry.io/)
- [Bugsink](https://www.bugsink.com/)
- [GlitchTip](https://glitchtip.com/)

### Setup

To enable this reporter, you must provide a Data Source Name (DSN) in your properties:

```yaml
lib-exception:
  error-reporting:
    enabled: true
    dsn: "http://your-key@sentry.example.com/1"
    environment: "production"
    minimum-status: 500
```

*Note: Performance tracing is intentionally disabled (`tracesSampleRate=0.0`) in the `SentryProtocolErrorReporter` because platforms like Bugsink and GlitchTip do not implement the performance-tracing backend. This library focuses purely on error capturing.*

## Thresholds

The `minimum-status` property dictates which exceptions get sent to the bug tracker.
By default, it is set to `500`. This means:
- `NotFoundException` (404) will **not** be reported.
- A generic unhandled `NullPointerException` (500) **will** be reported.

If you wish to track 4xx errors (for example, to identify frequent client-side issues), you can lower this value to `400`.

## No-Op Reporter

If `lib-exception.error-reporting.dsn` is missing, or `enabled` is set to `false`, a `NoopErrorReporter` is loaded into the context instead. In this scenario, exceptions will still be logged to standard out (or your log file), but no network calls will be made to an external tracker.
