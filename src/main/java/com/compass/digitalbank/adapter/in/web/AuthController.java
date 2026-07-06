package com.compass.digitalbank.adapter.in.web;

import com.compass.digitalbank.adapter.in.web.dto.AuthResponse;
import com.compass.digitalbank.adapter.in.web.dto.LoginRequest;
import com.compass.digitalbank.adapter.in.web.dto.RegisterRequest;
import com.compass.digitalbank.domain.port.in.AuthUseCase;
import com.compass.digitalbank.domain.port.in.AuthUseCase.AuthenticateCommand;
import com.compass.digitalbank.domain.port.in.AuthUseCase.RegisterCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user and issue an access token")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        AuthUseCase.AuthResult result = authUseCase.register(
                new RegisterCommand(request.name(), request.email(), request.password()));
        return AuthResponse.from(result);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate an existing user and issue an access token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AuthUseCase.AuthResult result = authUseCase.authenticate(
                new AuthenticateCommand(request.email(), request.password()));
        return AuthResponse.from(result);
    }
}
