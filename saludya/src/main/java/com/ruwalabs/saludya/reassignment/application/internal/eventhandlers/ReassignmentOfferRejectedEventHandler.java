package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link ReassignmentOfferRejectedEvent}.
 *
 * <p>When a candidate rejects the offer, the same freed slot is offered to the next
 * candidate in the booking queue.</p>
 */
@Service
@Slf4j
public class ReassignmentOfferRejectedEventHandler {

    private final ReassignmentCommandService reassignmentCommandService;

    public ReassignmentOfferRejectedEventHandler(ReassignmentCommandService reassignmentCommandService) {
        this.reassignmentCommandService = reassignmentCommandService;
    }

    /**
     * Reacts to {@link ReassignmentOfferRejectedEvent} by offering the slot to the next candidate.
     *
     * @param event the offer-rejected event
     */
    @EventListener
    public void on(ReassignmentOfferRejectedEvent event) {
        log.info("Offer {} rejected; offering freed slot {} to the next candidate",
                event.offerId(), event.freedTimeSlotId());
        reassignmentCommandService.sendReassignmentOffer(new SendReassignmentOfferCommand(
                event.originalAppointmentId(),
                event.freedTimeSlotId()));
    }
}
