package com.compass.digitalbank.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = ValidationConstants.NAME_MAX) String name,

        @NotBlank
        @Size(max = ValidationConstants.EMAIL_MAX)
        @Pattern(regexp = ValidationConstants.EMAIL_PATTERN, message = ValidationConstants.EMAIL_PATTERN_MESSAGE)
        String email,

        @NotBlank
        @Size(min = ValidationConstants.PASSWORD_MIN, max = ValidationConstants.PASSWORD_MAX)
        @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = ValidationConstants.PASSWORD_PATTERN_MESSAGE)
        String password
) {
}
