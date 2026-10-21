package official.onncallbackend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Standard API error response payload.
 * Provides consistent error details across all HTTP errors:
 * - status: HTTP status code (e.g. 400, 404, 409, 500)
 * - error: Reason phrase (e.g. "Bad Request", "Not Found", "Conflict")
 * - message: Human-readable error description
 * - path: The request URI where the error occurred
 * - timestamp: ISO-8601 instant when the error occurred
 * - fields: Map of field-level validation messages (present on 400 Bad Request)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Integer status,
        String error,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> fields
) {
    public ErrorResponse(String error, String message, String path, Instant timestamp, Map<String, String> fields) {
        this(null, error, message, path, timestamp, fields);
    }
}
