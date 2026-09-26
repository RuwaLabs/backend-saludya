package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferExpiredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link ReassignmentOfferExpiredEvent}.
 *
 * <p>When a candidate does not respond within the window, the same freed slot is offered
 * to the next candidate in the booking queue.</p>
 */
@Service
@Slf4j
public class ReassignmentOfferExpiredEventHandler {

    private final ReassignmentCommandService reassignmentCommandService;

    public ReassignmentOfferExpiredEventHandler(ReassignmentCommandService reassignmentCommandService) {
        this.reassignmentCommandService = reassignmentCommandService;
    }

    /**
     * Reacts to {@link ReassignmentOfferExpiredEvent} by offering the slot to the next candidate.
     *
     * @param event the offer-expired event
     */
    @EventListener
    public void on(ReassignmentOfferExpiredEvent event) {
        log.info("Offer {} expired; offering freed slot {} to the next candidate",
                event.offerId(), event.freedTimeSlotId());
        reassignmentCommandService.sendReassignmentOffer(new SendReassignmentOfferCommand(
                event.originalAppointmentId(),
                event.freedTimeSlotId()));
    }
}
