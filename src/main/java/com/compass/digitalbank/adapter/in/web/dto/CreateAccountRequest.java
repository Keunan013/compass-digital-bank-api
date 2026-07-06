package com.compass.digitalbank.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = ValidationConstants.ACCOUNT_NAME_MAX) String name,

        @NotNull
        @DecimalMin(value = ValidationConstants.MIN_INITIAL_BALANCE)
        @Digits(integer = ValidationConstants.AMOUNT_INTEGER_DIGITS, fraction = ValidationConstants.AMOUNT_FRACTION_DIGITS)
        BigDecimal initialBalance
) {
}
