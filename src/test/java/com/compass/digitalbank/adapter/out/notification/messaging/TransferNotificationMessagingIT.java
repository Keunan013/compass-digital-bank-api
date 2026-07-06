package com.compass.digitalbank.adapter.out.notification.messaging;

import com.compass.digitalbank.domain.event.TransferCompletedEvent;
import com.compass.digitalbank.domain.model.Account;
import com.compass.digitalbank.domain.model.Requester;
import com.compass.digitalbank.domain.model.Role;
import com.compass.digitalbank.domain.model.User;
import com.compass.digitalbank.domain.port.in.TransferUseCase;
import com.compass.digitalbank.domain.port.in.TransferUseCase.TransferCommand;
import com.compass.digitalbank.domain.port.out.AccountRepository;
import com.compass.digitalbank.domain.port.out.NotificationPort;
import com.compass.digitalbank.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest(properties = {
        "app.seed.enabled=false",
        "app.rate-limit.enabled=false",
        "app.messaging.enabled=true"
})
class TransferNotificationMessagingIT {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");
    private static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("25.00");
    private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(15);

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @SuppressWarnings("resource")
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @SpyBean
    private NotificationPort notificationPort;

    @Autowired
    private TransferUseCase transferUseCase;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void deliversNotificationThroughTheMessageBroker() {
        User owner = userRepository.save(User.create("Owner", uniqueEmail(), "hash", Role.USER));
        Account source = accountRepository.save(Account.open(owner.getId(), "Source", INITIAL_BALANCE));
        Account destination = accountRepository.save(Account.open(owner.getId(), "Destination", BigDecimal.ZERO));

        transferUseCase.transfer(new TransferCommand(
                new Requester(owner.getId(), false), source.getId(), destination.getId(), TRANSFER_AMOUNT, null));

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                verify(notificationPort).notifyTransferCompleted(any(TransferCompletedEvent.class)));
    }

    private String uniqueEmail() {
        return "owner-" + UUID.randomUUID() + "@example.com";
    }
}
