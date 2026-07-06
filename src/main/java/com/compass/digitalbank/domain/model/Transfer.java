package com.compass.digitalbank.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Transfer {

    private final UUID id;
    private final UUID sourceAccountId;
    private final UUID destinationAccountId;
    private final BigDecimal amount;
    private final TransferStatus status;
    private final String idempotencyKey;
    private final Instant createdAt;

    private Transfer(UUID id, UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount,
                     TransferStatus status, String idempotencyKey, Instant createdAt) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public static Transfer completed(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount,
                                     String idempotencyKey) {
        return new Transfer(null, sourceAccountId, destinationAccountId, amount,
                TransferStatus.COMPLETED, idempotencyKey, null);
    }

    public static Transfer restore(UUID id, UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount,
                                   TransferStatus status, String idempotencyKey, Instant createdAt) {
        return new Transfer(id, sourceAccountId, destinationAccountId, amount, status, idempotencyKey, createdAt);
    }

    public TransactionDirection directionFor(UUID accountId) {
        return sourceAccountId.equals(accountId) ? TransactionDirection.DEBIT : TransactionDirection.CREDIT;
    }

    public UUID counterpartyFor(UUID accountId) {
        return sourceAccountId.equals(accountId) ? destinationAccountId : sourceAccountId;
    }
}
