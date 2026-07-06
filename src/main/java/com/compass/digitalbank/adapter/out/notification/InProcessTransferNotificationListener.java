package com.compass.digitalbank.adapter.out.notification;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;
import com.compass.digitalbank.domain.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "false")
@RequiredArgsConstructor
public class InProcessTransferNotificationListener {

    private final NotificationPort notificationPort;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransferCompletedEvent event) {
        try {
            notificationPort.notifyTransferCompleted(event);
        } catch (RuntimeException ex) {
            log.error("Failed to deliver notification for transfer {}", event.transferId(), ex);
        }
    }
}
