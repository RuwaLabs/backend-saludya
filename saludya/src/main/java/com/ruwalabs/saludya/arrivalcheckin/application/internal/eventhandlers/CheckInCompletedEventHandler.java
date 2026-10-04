package com.ruwalabs.saludya.arrivalcheckin.application.internal.eventhandlers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.CheckInCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link CheckInCompletedEvent} for auditing.
 */
@Service
@Slf4j
public class CheckInCompletedEventHandler {

    @EventListener
    public void on(CheckInCompletedEvent event) {
        log.info("Check-in {} completed for appointment {} at queue {} position {}",
                event.checkInId(), event.appointmentId(), event.attendanceQueueId(), event.position());
    }
}
