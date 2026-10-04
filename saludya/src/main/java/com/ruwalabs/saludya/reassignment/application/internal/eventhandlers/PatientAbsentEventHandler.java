package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.PatientAbsentEvent;
import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for the Arrival context's {@link PatientAbsentEvent}.
 *
 * <p>This is the single entry point of the reassignment flow. When a patient is declared
 * absent in the {@code Arrival & QR Check-in} context, the freed time slot triggers the
 * reassignment chain. Note: booking cancellations do <b>not</b> trigger reassignment.</p>
 */
@Service
@Slf4j
public class PatientAbsentEventHandler {

    private final ReassignmentCommandService reassignmentCommandService;

    public PatientAbsentEventHandler(ReassignmentCommandService reassignmentCommandService) {
        this.reassignmentCommandService = reassignmentCommandService;
    }

    @EventListener
    public void on(PatientAbsentEvent event) {
        if (event.freedTimeSlotId() == null) {
            log.info("Absence for appointment {} has no freed time slot; skipping reassignment", event.appointmentId());
            return;
        }
        log.info("Freed time slot {} due to absent appointment {}; starting reassignment",
                event.freedTimeSlotId(), event.appointmentId());
        reassignmentCommandService.sendReassignmentOffer(new SendReassignmentOfferCommand(
                event.appointmentId(),
                event.freedTimeSlotId()));
    }
}
