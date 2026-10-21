package official.onncallbackend.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import official.onncallbackend.Auth.Security.JwtAuthFilter;
import official.onncallbackend.Auth.Service.JwtService;
import official.onncallbackend.common.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Coarse rules by URL and role. Record-level rules (is this your client profile?)
 * are enforced in the services through AccessGuard.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtService jwtService,
                          ObjectMapper objectMapper,
                          @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/otp/**", "/api/auth/login", "/api/auth/register",
                                "/api/auth/firebase/**").permitAll()
                        .requestMatchers("/actuator/health", "/h2-console/**", "/error").permitAll()
                        .requestMatchers("/api/users", "/api/users/**").permitAll()

                        // Service catalogue: public to read, admin to change
                        .requestMatchers(HttpMethod.GET, "/api/services", "/api/services/**").permitAll()
                        .requestMatchers("/api/services", "/api/services/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/clients", "/api/distributors").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/clients", "/api/providers", "/api/distributors",
                                "/api/notifications", "/api/job-status-history").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/clients/**", "/api/providers/**", "/api/distributors/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeError(res, HttpStatus.UNAUTHORIZED, "Authentication required", req))
                        .accessDeniedHandler((req, res, e) ->
                                writeError(res, HttpStatus.FORBIDDEN, "Access denied", req)))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void writeError(HttpServletResponse res, HttpStatus status, String message,
                            HttpServletRequest req) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getOutputStream(), new ErrorResponse(
                status.getReasonPhrase(), message, req.getRequestURI(), Instant.now(), null));
    }
}
