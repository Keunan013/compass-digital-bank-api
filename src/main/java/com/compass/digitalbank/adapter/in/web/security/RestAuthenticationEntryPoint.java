package com.compass.digitalbank.adapter.in.web.security;

import com.compass.digitalbank.adapter.in.web.error.ErrorResponseWriter;
import com.compass.digitalbank.domain.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_CREDENTIALS.name(), "Authentication is required to access this resource");
    }
}
