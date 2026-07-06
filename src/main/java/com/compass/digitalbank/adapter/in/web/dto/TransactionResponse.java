package com.compass.digitalbank.adapter.in.web.dto;

import com.compass.digitalbank.domain.model.TransactionDirection;
import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transferId,
        TransactionDirection direction,
        UUID counterpartyAccountId,
        BigDecimal amount,
        TransferStatus status,
        Instant createdAt
) {

    public static TransactionResponse from(Transfer transfer, UUID accountId) {
        return new TransactionResponse(
                transfer.getId(),
                transfer.directionFor(accountId),
                transfer.counterpartyFor(accountId),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getCreatedAt());
    }
}
