package official.onncallbackend.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP Request Logging Filter.
 * 
 * Equivalent to Express.js global middleware:
 *   app.use((req, res, next) => { ... })
 * 
 * Extends Spring's OncePerRequestFilter to guarantee execution exactly once
 * per request dispatch within a single request thread, preventing duplicate logs
 * during internal servlet forwards or error dispatches.
 * 
 * Logs:
 * - HTTP method (e.g. GET, POST)
 * - Request URI (e.g. /api/users)
 * - HTTP response status code (e.g. 200, 201, 400, 404, 409)
 * - Request execution duration in milliseconds
 * 
 * Safe by design: does not inspect or log request/response bodies, authorization
 * headers, passwords, cookies, or any sensitive credentials.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Target only API requests; skip static assets, error pages, and actuator health checks
        return !path.startsWith("/api");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        try {
            // Forward request down the filter chain to controllers and exception handlers
            filterChain.doFilter(request, response);
        } finally {
            // Executed unconditionally after the response has been rendered or exception handled
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            log.info("HTTP {} {} -> status={} (took {}ms)", method, uri, status, duration);
        }
    }
}
