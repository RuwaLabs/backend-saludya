package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.interfaces.events.PatientAbsentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link PatientAbsentEvent}.
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

    /**
     * Reacts to {@link PatientAbsentEvent} by starting the reassignment chain.
     *
     * @param event the patient-absent integration event
     */
    @EventListener
    public void on(PatientAbsentEvent event) {
        log.info("Freed time slot {} due to absent appointment {}; starting reassignment",
                event.freedTimeSlotId(), event.appointmentId());
        reassignmentCommandService.sendReassignmentOffer(new SendReassignmentOfferCommand(
                event.appointmentId(),
                event.freedTimeSlotId()));
    }
}
