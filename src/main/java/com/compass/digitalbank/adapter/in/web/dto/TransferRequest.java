package com.compass.digitalbank.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceAccountId,

        @NotNull UUID destinationAccountId,

        @NotNull
        @DecimalMin(value = ValidationConstants.MIN_TRANSFER_AMOUNT)
        @Digits(integer = ValidationConstants.AMOUNT_INTEGER_DIGITS, fraction = ValidationConstants.AMOUNT_FRACTION_DIGITS)
        BigDecimal amount
) {
}
