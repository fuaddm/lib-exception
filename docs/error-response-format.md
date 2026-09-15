# Error response format

Every service using this library returns the same JSON shape (`ErrorResponse`) for
every error, whether it came from your own thrown exception, a Bean Validation
failure, or a genuine unhandled bug.

```json
{
  "timestamp": "2026-09-14T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "PIN_NOT_FOUND",
  "message": "Pin 42 not found",
  "path": "/api/pins/42",
  "traceId": "9f1c2e6b8a3d4c5e",
  "errors": null
}
```

Fields (see `dto/ErrorResponse.java`):

| Field       | Type              | Notes |
|-------------|-------------------|-------|
| `timestamp` | ISO-8601 string   | Set automatically, server time |
| `status`    | int               | HTTP status code, e.g. `404` |
| `error`     | string            | HTTP reason phrase, e.g. `"Not Found"` |
| `code`      | string            | Stable machine-readable code from an `ErrorCode` — safe to `switch`/`if` on |
| `message`   | string            | Human-readable; safe to log or show to a developer |
| `path`      | string            | Request URI that produced the error |
| `traceId`   | string, nullable  | Correlation id — see below |
| `errors`    | array, nullable   | Field-level validation failures (see below); `null` unless it's a validation error |

Fields with a `null` value are **omitted** from the JSON entirely
(`@JsonInclude(NON_NULL)`), not sent as `"field": null` — so `traceId` and `errors`
simply won't appear in the payload when there's nothing to put there.

## Validation errors

When a request fails Bean Validation or a `ValidationException` is thrown, `errors`
is populated:

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "One or more fields failed validation.",
  "path": "/api/users",
  "errors": [
    { "field": "email", "message": "must be a well-formed email address", "rejectedValue": "not-an-email" },
    { "field": "username", "message": "must not be blank", "rejectedValue": "" }
  ]
}
```

## `traceId` — wiring cross-service correlation

`traceId` is read from SLF4J's MDC key `"traceId"` at the moment the error is
handled. The library does **not** set this key itself — it just reads whatever is
already there. To get correlation IDs across your microservices:

1. Generate/propagate a trace id at your gateway (or the first service in a
   request chain) — e.g. read an incoming `X-Trace-Id` header, or generate a UUID
   if absent.
2. Put it in MDC early in the request lifecycle (a `Filter`/`HandlerInterceptor` in
   each service):
   ```java
   MDC.put("traceId", traceId);
   ```
3. Propagate the same header (`X-Trace-Id`) on any outbound call to another
   microservice (Feign/RestTemplate/WebClient interceptor).
4. Clear it at the end of the request (`MDC.remove("traceId")` in a `finally`, or
   Spring's `RequestContextFilter`/servlet lifecycle if you're already using one).

Until you wire this, `traceId` simply won't appear in error responses — it's
optional and additive, not required for the rest of the library to work.

## Consuming this from another service (client side)

Any service calling another (Feign/RestTemplate/WebClient) can deserialize the
error body straight into `com.example.exceptionlib.dto.ErrorResponse` if it also
depends on this library — the shape is identical everywhere. Note this covers
**inbound** handling only (turning your own thrown exceptions into this JSON); this
library does not currently include an outbound decoder that automatically rethrows
a typed exception from another service's `ErrorResponse` — see
[design-decisions.md](./design-decisions.md#outbound-error-handling-not-included-yet)
for why, and ask if you want that layer built.
