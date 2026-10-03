package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.internal.eventhandlers;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.events.ConfigurationUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles configuration update events.
 *
 * This handler is intentionally lightweight until consuming bounded
 * contexts expose their own cache/audit integration ports.
 */
@Component
public class ConfigurationUpdatedEventHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ConfigurationUpdatedEventHandler.class);

    @EventListener
    public void handle(ConfigurationUpdatedEvent event) {
        LOGGER.info(
                "Hospital configuration updated. configurationId={}, occurredAt={}",
                event.configurationId(),
                event.occurredAt()
        );

        /*
         * Future integrations:
         *
         * - invalidate/update booking configuration cache
         * - invalidate/update check-in configuration cache
         * - register audit entry
         */
    }
}