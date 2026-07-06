package com.compass.digitalbank.adapter.in.web.dto;

import com.compass.digitalbank.domain.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID ownerId,
        String name,
        BigDecimal balance,
        boolean active,
        Instant createdAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerId(),
                account.getName(),
                account.getBalance(),
                account.isActive(),
                account.getCreatedAt());
    }
}
