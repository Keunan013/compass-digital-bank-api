package com.compass.digitalbank.adapter.out.notification.messaging;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferNotificationMessage(
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        BigDecimal sourceBalanceAfter,
        String recipientName,
        String recipientEmail,
        Instant occurredAt
) {

    public static TransferNotificationMessage from(TransferCompletedEvent event) {
        return new TransferNotificationMessage(
                event.transferId(),
                event.sourceAccountId(),
                event.destinationAccountId(),
                event.amount(),
                event.sourceBalanceAfter(),
                event.recipientName(),
                event.recipientEmail(),
                event.occurredAt());
    }

    public TransferCompletedEvent toEvent() {
        return new TransferCompletedEvent(
                transferId,
                sourceAccountId,
                destinationAccountId,
                amount,
                sourceBalanceAfter,
                recipientName,
                recipientEmail,
                occurredAt);
    }
}
