package com.compass.digitalbank.adapter.in.web.security;

import com.compass.digitalbank.adapter.in.web.error.ErrorResponseWriter;
import com.compass.digitalbank.domain.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorResponseWriter.write(request, response, HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED.name(), "You do not have permission to access this resource");
    }
}
