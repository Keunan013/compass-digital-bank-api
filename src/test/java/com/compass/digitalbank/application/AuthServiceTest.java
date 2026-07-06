package com.compass.digitalbank.application;

import com.compass.digitalbank.domain.exception.EmailAlreadyUsedException;
import com.compass.digitalbank.domain.exception.InvalidCredentialsException;
import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.in.AuthUseCase.AuthResult;
import com.compass.digitalbank.domain.port.in.AuthUseCase.AuthenticateCommand;
import com.compass.digitalbank.domain.port.in.AuthUseCase.RegisterCommand;
import com.compass.digitalbank.domain.port.out.PasswordHasher;
import com.compass.digitalbank.domain.port.out.TokenProvider;
import com.compass.digitalbank.domain.port.out.TokenProvider.IssuedToken;
import com.compass.digitalbank.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "alice@example.com";
    private static final String RAW_PASSWORD = "Password123!";
    private static final String PASSWORD_HASH = "hashed-password";
    private static final String ACCESS_TOKEN = "access-token";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User storedUser() {
        return User.restore(UUID.randomUUID(), "Alice", EMAIL, PASSWORD_HASH, Role.USER, Instant.now());
    }

    @Test
    void registersNewUserAndIssuesToken() {
        User saved = storedUser();
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(tokenProvider.issue(saved)).thenReturn(new IssuedToken(ACCESS_TOKEN, 3600));

        AuthResult result = authService.register(new RegisterCommand("Alice", EMAIL, RAW_PASSWORD));

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.userId()).isEqualTo(saved.getId());
        assertThat(result.role()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsRegistrationForExistingEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterCommand("Alice", EMAIL, RAW_PASSWORD)))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticatesWithValidCredentials() {
        User user = storedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(tokenProvider.issue(user)).thenReturn(new IssuedToken(ACCESS_TOKEN, 3600));

        AuthResult result = authService.authenticate(new AuthenticateCommand(EMAIL, RAW_PASSWORD));

        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void rejectsAuthenticationWithWrongPassword() {
        User user = storedUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordHasher.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(new AuthenticateCommand(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsAuthenticationForUnknownEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(new AuthenticateCommand(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
