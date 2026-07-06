package com.compass.digitalbank.adapter.in.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final TraceIdProvider traceIdProvider;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String code, String message) throws IOException {
        ApiError body = ApiError.of(status.value(), code, message,
                request.getRequestURI(), traceIdProvider.currentTraceId());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
