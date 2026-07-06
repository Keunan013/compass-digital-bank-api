package com.compass.digitalbank.domain.port.out;

public interface DomainEventPublisher {

    void publish(Object event);
}
