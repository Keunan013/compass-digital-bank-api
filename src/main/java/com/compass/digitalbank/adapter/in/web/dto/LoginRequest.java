package com.compass.digitalbank.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank
        @Pattern(regexp = ValidationConstants.EMAIL_PATTERN, message = ValidationConstants.EMAIL_PATTERN_MESSAGE)
        String email,

        @NotBlank String password
) {
}
