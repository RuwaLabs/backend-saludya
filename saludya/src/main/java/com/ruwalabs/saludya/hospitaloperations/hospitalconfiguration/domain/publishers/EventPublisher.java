package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.publishers;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.events.DomainEvent;

/**
 * Port used by the application layer to publish domain events.
 */
public interface EventPublisher {

    void publish(DomainEvent event);
}