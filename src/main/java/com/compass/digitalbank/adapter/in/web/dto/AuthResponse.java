package com.compass.digitalbank.adapter.in.web.dto;

import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.port.in.AuthUseCase.AuthResult;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String name,
        String email,
        Role role
) {

    private static final String TOKEN_TYPE = "Bearer";

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.accessToken(),
                TOKEN_TYPE,
                result.expiresInSeconds(),
                result.userId(),
                result.name(),
                result.email(),
                result.role());
    }
}
