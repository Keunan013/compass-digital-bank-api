package com.compass.digitalbank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMqConfig {

    public static final String NOTIFICATIONS_EXCHANGE = "transfer.notifications";
    public static final String DEAD_LETTER_EXCHANGE = "transfer.notifications.dlx";
    public static final String TRANSFER_COMPLETED_ROUTING_KEY = "transfer.completed";
    public static final String TRANSFER_COMPLETED_QUEUE = "transfer.notifications.transfer-completed";
    public static final String TRANSFER_COMPLETED_DLQ = "transfer.notifications.transfer-completed.dlq";

    private static final String DEAD_LETTER_EXCHANGE_ARG = "x-dead-letter-exchange";
    private static final String DEAD_LETTER_ROUTING_KEY_ARG = "x-dead-letter-routing-key";

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(NOTIFICATIONS_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue transferCompletedQueue() {
        return QueueBuilder.durable(TRANSFER_COMPLETED_QUEUE)
                .withArgument(DEAD_LETTER_EXCHANGE_ARG, DEAD_LETTER_EXCHANGE)
                .withArgument(DEAD_LETTER_ROUTING_KEY_ARG, TRANSFER_COMPLETED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue transferCompletedDeadLetterQueue() {
        return QueueBuilder.durable(TRANSFER_COMPLETED_DLQ).build();
    }

    @Bean
    public Binding transferCompletedBinding() {
        return BindingBuilder.bind(transferCompletedQueue())
                .to(notificationsExchange())
                .with(TRANSFER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(transferCompletedDeadLetterQueue())
                .to(deadLetterExchange())
                .with(TRANSFER_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
