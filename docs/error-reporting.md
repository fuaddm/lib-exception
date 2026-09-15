# Error reporting (Bugsink / Sentry-protocol)

`GlobalExceptionHandler` can forward exceptions to any server that speaks the
Sentry event-ingestion protocol — Sentry itself, self-hosted **Bugsink**,
GlitchTip, etc. This uses the official `io.sentry:sentry` Java SDK internally, but
nothing in the library is Bugsink-specific — it's just pointed at your DSN.

By default this is **off** (a `NoopErrorReporter` is used) until a service
configures a DSN.

The actual Bugsink instance for this project is at
`http://coolify.tail1270a7.ts.net:8001` (see [infrastructure.md](./infrastructure.md)) —
project `1` is the one used so far. The `<key>` in the DSN is a write-only ingest
credential; it lives wherever you're storing secrets (Vault, a CI secret store),
never hardcoded in a service's committed config.

## Enabling it in a microservice

In that service's `application.yml`:

```yaml
exception-lib:
  error-reporting:
    dsn: ${BUGSINK_DSN}          # e.g. http://<key>@coolify.tail1270a7.ts.net:8001/1
    environment: production      # optional, shown as the "environment" tag in Bugsink
    minimum-status: 500          # optional, default 500 — only server errors are forwarded
    enabled: true                # optional, default true (set false to disable without removing the dsn)
```

Or `application.properties`:
```properties
exception-lib.error-reporting.dsn=${BUGSINK_DSN}
exception-lib.error-reporting.environment=production
```

**Don't commit the raw DSN to version control.** It's a write-only ingest key (it
can't be used to read your data back out), but anyone with it can submit fake
events into your project. Inject it via an environment variable, Kubernetes
secret, or your platform's secret manager, and reference it with `${BUGSINK_DSN}`
as shown above.

## What gets reported

| Source | Reported? |
|---|---|
| `NotFoundException`, `ConflictException`, `BadRequestException`, etc. (4xx) | No, by default |
| Any `BaseException` whose `ErrorCode` maps to `minimum-status` or higher | Yes |
| Unhandled/unexpected exceptions (falls through to the generic `Exception` handler → always 500) | Yes, always |

The threshold exists so routine business exceptions (a pin not found, a duplicate
username) don't flood your Bugsink project — only genuine bugs/outages do, unless
you lower `minimum-status`.

## Verifying it's actually reaching your Bugsink instance

The library ships an opt-in connectivity test (skipped by default, so it never
touches the network during normal builds/CI):

```bash
BUGSINK_SMOKE_TEST=true BUGSINK_DSN="http://<key>@host:port/<project>" \
  ./gradlew test --tests "*SentryProtocolErrorReporterSmokeTest*"
```

It sends one real test exception and prints the resulting event id — look for that
id in your Bugsink project UI to confirm delivery.

## Multiple microservices, one Bugsink project vs. several

You can either:
- **Share one DSN** across all Pinterest-clone services (simplest), and rely on
  `environment`/the exception's package name to tell them apart in the Bugsink UI, or
- **Create a separate Bugsink project per microservice** and give each its own DSN
  — cleaner separation, recommended once you have more than a couple of services.

Nothing in the library assumes either way; it's purely which DSN you put in each
service's config.
