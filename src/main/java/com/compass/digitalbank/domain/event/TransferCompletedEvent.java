package com.compass.digitalbank.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        BigDecimal sourceBalanceAfter,
        String recipientName,
        String recipientEmail,
        Instant occurredAt
) {
}
