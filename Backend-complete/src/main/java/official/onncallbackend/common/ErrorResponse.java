package official.onncallbackend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Every error the API returns has this shape:
 * { "error": "Not Found", "message": "...", "path": "/api/...", "timestamp": "...", "fields": {...} }
 * "fields" is only present for validation errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String error,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> fields
) {
}
