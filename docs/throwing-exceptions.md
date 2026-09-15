# Throwing exceptions

## The hierarchy

Everything extends `BaseException`, which carries an `ErrorCode`:

| Exception                  | HTTP status | Default `CommonErrorCode`   |
|-----------------------------|:-----------:|------------------------------|
| `BadRequestException`       | 400         | `BAD_REQUEST`                |
| `ValidationException`       | 400         | `VALIDATION_FAILED`          |
| `UnauthorizedException`     | 401         | `UNAUTHORIZED`                |
| `ForbiddenException`        | 403         | `FORBIDDEN`                  |
| `NotFoundException`         | 404         | `RESOURCE_NOT_FOUND`         |
| `ConflictException`         | 409         | `CONFLICT`                   |
| `TooManyRequestsException`  | 429         | `TOO_MANY_REQUESTS`          |
| *(uncaught `Exception`)*    | 500         | `INTERNAL_SERVER_ERROR`      |

You never have to write a try/catch or a controller-level `@ExceptionHandler` for
these — throw them from anywhere (controller, service, repository) and
`GlobalExceptionHandler` (auto-registered from this library) turns them into the
[standard JSON error body](./error-response-format.md).

## Simplest usage — just a message

```java
throw new NotFoundException("Pin 42 not found");
throw new ConflictException("Username already taken");
throw new ForbiddenException("You don't own this board");
```

This is fine for one-off cases where you don't need a stable machine-readable code.

## Recommended usage — your own `ErrorCode` enum

For anything a client (your frontend, mobile app, or another microservice) might
want to branch on programmatically, define a domain-specific enum per service:

```java
// in the Pin service
public enum PinErrorCode implements ErrorCode {

    PIN_NOT_FOUND(HttpStatus.NOT_FOUND, "Pin could not be found."),
    PIN_ALREADY_SAVED(HttpStatus.CONFLICT, "Pin is already saved to this board."),
    BOARD_LIMIT_REACHED(HttpStatus.FORBIDDEN, "This board has reached its pin limit.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    PinErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    @Override public String getCode() { return name(); }
    @Override public HttpStatus getHttpStatus() { return httpStatus; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
```

Then throw it through whichever `BaseException` subclass matches the *kind* of
failure (the subclass is mostly a convenience/readability wrapper — the actual
HTTP status returned comes from your `ErrorCode`, not the exception class name):

```java
throw new NotFoundException(PinErrorCode.PIN_NOT_FOUND);
// or with a specific message instead of the enum's default:
throw new NotFoundException(PinErrorCode.PIN_NOT_FOUND, "Pin " + pinId + " not found");
```

Resulting JSON:
```json
{
  "status": 404,
  "error": "Not Found",
  "code": "PIN_NOT_FOUND",
  "message": "Pin 42 not found",
  "path": "/api/pins/42"
}
```

**Naming convention:** keep each service's codes prefixed or scoped mentally by
service (`PIN_NOT_FOUND`, `USER_NOT_FOUND`, `BOARD_LIMIT_REACHED`) since `code` is a
flat string in the response — there's no namespacing enforced by the library.
`code` is part of your public API contract once shipped: don't rename existing
values, only add new ones.

## Bean Validation (`@Valid`) — you don't need to do anything

`MethodArgumentNotValidException` (from `@Valid @RequestBody`) and
`ConstraintViolationException` (from `@Validated` on path/query params) are already
handled by `GlobalExceptionHandler` and turned into a 400 with a populated
`errors` array — see [error-response-format.md](./error-response-format.md).

## Cross-field / business-rule validation

For validation failures that Bean Validation annotations can't express (e.g. "end
date must be after start date"), use `ValidationException` directly with a list of
`FieldErrorDetail`:

```java
throw new ValidationException(List.of(
    FieldErrorDetail.builder()
        .field("endDate")
        .message("must be after startDate")
        .rejectedValue(request.getEndDate())
        .build()
));
```

## Overriding the default handling in one service

`GlobalExceptionHandler` runs at `Ordered.LOWEST_PRECEDENCE`. If a service needs
different behavior for a specific exception, just define its own
`@RestControllerAdvice` with a more specific/higher-precedence handler for that
exception type — Spring will prefer it over this library's fallback. You don't need
to (and shouldn't) disable or replace the shared handler for one-off cases.
