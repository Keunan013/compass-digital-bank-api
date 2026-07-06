package com.compass.digitalbank.adapter.in.web.ratelimit;

import com.compass.digitalbank.adapter.in.web.error.ErrorResponseWriter;
import com.compass.digitalbank.domain.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PREFIXES =
            List.of("/swagger-ui", "/v3/api-docs", "/actuator");
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String FORWARDED_FOR_DELIMITER = ",";

    private final RateLimiterService rateLimiterService;
    private final ErrorResponseWriter errorResponseWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientKey = resolveClientKey(request);
        if (rateLimiterService.tryConsume(clientKey)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.warn("Rate limit exceeded for client {} on {}", clientKey, request.getRequestURI());
        errorResponseWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
                ErrorCode.RATE_LIMIT_EXCEEDED.name(), "Rate limit exceeded, please retry later");
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(FORWARDED_FOR_DELIMITER)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
