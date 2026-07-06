package com.compass.digitalbank.adapter.out.security;

import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.out.TokenProvider.IssuedToken;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256";
    private static final String ISSUER = "digital-bank-api";
    private static final long EXPIRATION_MINUTES = 60;

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, EXPIRATION_MINUTES, ISSUER));

    private User user() {
        return User.restore(UUID.randomUUID(), "Alice", "alice@example.com", "hash", Role.ADMIN, Instant.now());
    }

    @Test
    void issuesAndVerifiesToken() {
        User user = user();

        IssuedToken token = jwtService.issue(user);
        JwtService.AuthenticatedUser authenticated = jwtService.verify(token.value());

        assertThat(token.expiresInSeconds()).isEqualTo(EXPIRATION_MINUTES * 60);
        assertThat(authenticated.id()).isEqualTo(user.getId());
        assertThat(authenticated.email()).isEqualTo(user.getEmail());
        assertThat(authenticated.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void rejectsTamperedToken() {
        String tampered = jwtService.issue(user()).value() + "tampered";

        assertThatThrownBy(() -> jwtService.verify(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        JwtService otherIssuer = new JwtService(
                new JwtProperties("a-completely-different-secret-key-value-here", EXPIRATION_MINUTES, ISSUER));
        String foreignToken = otherIssuer.issue(user()).value();

        assertThatThrownBy(() -> jwtService.verify(foreignToken))
                .isInstanceOf(JwtException.class);
    }
}
