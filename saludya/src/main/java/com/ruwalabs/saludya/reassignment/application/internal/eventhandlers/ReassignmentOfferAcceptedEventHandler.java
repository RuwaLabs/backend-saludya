package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferAcceptedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link ReassignmentOfferAcceptedEvent}.
 *
 * <p>Advances the reassignment chain: when a candidate accepts and moves to the freed
 * slot, their original slot ({@code candidateTimeSlotId}) becomes the next freed slot.
 * The chain is re-triggered for that slot. The actual appointment move is performed by
 * the {@code Appointments & Booking} context when it consumes this event.</p>
 */
@Service
@Slf4j
public class ReassignmentOfferAcceptedEventHandler {

    private final ReassignmentCommandService reassignmentCommandService;

    public ReassignmentOfferAcceptedEventHandler(ReassignmentCommandService reassignmentCommandService) {
        this.reassignmentCommandService = reassignmentCommandService;
    }

    /**
     * Reacts to {@link ReassignmentOfferAcceptedEvent} by re-offering the candidate's
     * original slot.
     *
     * @param event the offer-accepted event
     */
    @EventListener
    public void on(ReassignmentOfferAcceptedEvent event) {
        log.info("Offer {} accepted; freeing candidate's original slot {} for the next reassignment",
                event.offerId(), event.candidateTimeSlotId());
        reassignmentCommandService.sendReassignmentOffer(new SendReassignmentOfferCommand(
                event.appointmentId(),
                event.candidateTimeSlotId()));
    }
}
