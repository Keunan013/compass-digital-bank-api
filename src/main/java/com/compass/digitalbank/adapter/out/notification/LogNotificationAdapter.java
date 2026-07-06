package com.compass.digitalbank.adapter.out.notification;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;
import com.compass.digitalbank.domain.port.out.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogNotificationAdapter implements NotificationPort {

    @Override
    public void notifyTransferCompleted(TransferCompletedEvent event) {
        log.info("Notification for {} <{}>: transfer {} of {} completed. Account {} balance is now {}.",
                event.recipientName(),
                event.recipientEmail(),
                event.transferId(),
                event.amount(),
                event.sourceAccountId(),
                event.sourceBalanceAfter());
    }
}
