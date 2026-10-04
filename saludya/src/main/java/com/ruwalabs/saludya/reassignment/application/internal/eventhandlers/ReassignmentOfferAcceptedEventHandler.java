package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.application.commandservices.ReassignmentCommandService;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferAcceptedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link ReassignmentOfferAcceptedEvent}.
 *
 * <p>Moves the candidate's appointment to the freed slot through the Booking facade and
 * then advances the reassignment chain: the candidate's original slot
 * ({@code candidateTimeSlotId}) becomes the next freed slot.</p>
 */
@Service
@Slf4j
public class ReassignmentOfferAcceptedEventHandler {

    private final ReassignmentCommandService reassignmentCommandService;
    private final AppointmentLookupService appointmentLookupService;

    public ReassignmentOfferAcceptedEventHandler(
            ReassignmentCommandService reassignmentCommandService,
            AppointmentLookupService appointmentLookupService) {
        this.reassignmentCommandService = reassignmentCommandService;
        this.appointmentLookupService = appointmentLookupService;
    }

    @EventListener
    public void on(ReassignmentOfferAcceptedEvent event) {
        log.info("Offer {} accepted; moving appointment {} to freed slot {}",
                event.offerId(), event.appointmentId(), event.freedTimeSlotId());
        appointmentLookupService.moveAppointment(event.appointmentId(), event.freedTimeSlotId());

        log.info("Re-offering the candidate's original slot {} for the next reassignment",
                event.candidateTimeSlotId());
        reassignmentCommandService.sendReassignmentOffer(new SendReassignmentOfferCommand(
                event.appointmentId(),
                event.candidateTimeSlotId()));
    }
}
