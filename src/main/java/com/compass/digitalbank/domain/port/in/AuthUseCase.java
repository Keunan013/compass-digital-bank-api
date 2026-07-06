package com.compass.digitalbank.domain.port.in;

import com.compass.digitalbank.domain.model.Role;

import java.util.UUID;

public interface AuthUseCase {

    AuthResult register(RegisterCommand command);

    AuthResult authenticate(AuthenticateCommand command);

    record RegisterCommand(String name, String email, String rawPassword) {
    }

    record AuthenticateCommand(String email, String rawPassword) {
    }

    record AuthResult(
            String accessToken,
            long expiresInSeconds,
            UUID userId,
            String name,
            String email,
            Role role
    ) {
    }
}
