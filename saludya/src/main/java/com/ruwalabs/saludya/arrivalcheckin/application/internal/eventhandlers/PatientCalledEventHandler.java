package com.ruwalabs.saludya.arrivalcheckin.application.internal.eventhandlers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.PatientCalledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link PatientCalledEvent} for auditing.
 */
@Service
@Slf4j
public class PatientCalledEventHandler {

    @EventListener
    public void on(PatientCalledEvent event) {
        log.info("Queue entry {} called at position {} in queue {}",
                event.queueEntryId(), event.position(), event.attendanceQueueId());
    }
}
