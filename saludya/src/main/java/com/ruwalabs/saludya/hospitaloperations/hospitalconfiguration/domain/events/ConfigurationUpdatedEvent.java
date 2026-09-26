package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.events;

import java.time.LocalDateTime;

/**
 * Domain event raised when the hospital configuration is updated.
 */
public record ConfigurationUpdatedEvent(
        Long configurationId,
        LocalDateTime occurredAt
) implements DomainEvent {

    public ConfigurationUpdatedEvent {
        if (configurationId == null) {
            throw new IllegalArgumentException("Configuration ID cannot be null");
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException("Event occurrence time cannot be null");
        }
    }
}