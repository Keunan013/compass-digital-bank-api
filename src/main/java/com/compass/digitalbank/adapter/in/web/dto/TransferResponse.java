package com.compass.digitalbank.adapter.in.web.dto;

import com.compass.digitalbank.domain.model.Transfer;
import com.compass.digitalbank.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        TransferStatus status,
        Instant createdAt
) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getCreatedAt());
    }
}
