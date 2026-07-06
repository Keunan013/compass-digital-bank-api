package com.compass.digitalbank.adapter.out.notification.messaging;

import com.compass.digitalbank.config.RabbitMqConfig;
import com.compass.digitalbank.domain.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TransferNotificationConsumer {

    private final NotificationPort notificationPort;

    @RabbitListener(queues = RabbitMqConfig.TRANSFER_COMPLETED_QUEUE)
    public void handle(TransferNotificationMessage message) {
        log.debug("Received transfer-completed message for transfer {}", message.transferId());
        notificationPort.notifyTransferCompleted(message.toEvent());
    }
}
