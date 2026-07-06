package com.compass.digitalbank.adapter.out.notification.messaging;

import com.compass.digitalbank.config.RabbitMqConfig;
import com.compass.digitalbank.domain.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TransferCompletedRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransferCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTIFICATIONS_EXCHANGE,
                    RabbitMqConfig.TRANSFER_COMPLETED_ROUTING_KEY,
                    TransferNotificationMessage.from(event));
            log.debug("Published transfer-completed message for transfer {}", event.transferId());
        } catch (AmqpException ex) {
            log.error("Failed to publish transfer-completed message for transfer {}", event.transferId(), ex);
        }
    }
}
