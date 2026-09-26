package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.events;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.events.DomainEvent;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.publishers.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring implementation of the domain event publisher.
 */
@Component
public class SpringEventPublisherImpl implements EventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventPublisherImpl(
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Domain event cannot be null"
            );
        }

        eventPublisher.publishEvent(event);
    }
}