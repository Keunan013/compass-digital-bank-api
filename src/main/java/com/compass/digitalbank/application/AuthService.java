package com.compass.digitalbank.application;

import com.compass.digitalbank.domain.exception.EmailAlreadyUsedException;
import com.compass.digitalbank.domain.exception.InvalidCredentialsException;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.in.AuthUseCase;
import com.compass.digitalbank.domain.port.out.PasswordHasher;
import com.compass.digitalbank.domain.port.out.TokenProvider;
import com.compass.digitalbank.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResult register(RegisterCommand command) {
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }
        User user = userRepository.save(
                User.register(command.name().trim(), email, passwordHasher.hash(command.rawPassword())));
        log.info("Registered user {}", user.getId());
        return toResult(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult authenticate(AuthenticateCommand command) {
        User user = userRepository.findByEmail(normalizeEmail(command.email()))
                .filter(candidate -> passwordHasher.matches(command.rawPassword(), candidate.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        log.info("Authenticated user {}", user.getId());
        return toResult(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResult toResult(User user) {
        TokenProvider.IssuedToken token = tokenProvider.issue(user);
        return new AuthResult(token.value(), token.expiresInSeconds(),
                user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
