package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.CheckInCompletedEvent;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the Arrival context's {@link CheckInCompletedEvent}.
 *
 * <p>When a reassigned candidate arrives and checks in, their accepted offer is marked
 * as {@code ATTENDED} so the no-show detection ignores it.</p>
 */
@Service("reassignmentCheckInCompletedEventHandler")
@Slf4j
public class CheckInCompletedEventHandler {

    private final ReassignmentOfferRepository reassignmentOfferRepository;

    public CheckInCompletedEventHandler(ReassignmentOfferRepository reassignmentOfferRepository) {
        this.reassignmentOfferRepository = reassignmentOfferRepository;
    }

    @EventListener
    public void on(CheckInCompletedEvent event) {
        reassignmentOfferRepository.findAcceptedByAppointment(event.appointmentId())
                .ifPresent(offer -> {
                    offer.markArrived();
                    reassignmentOfferRepository.save(offer);
                    log.info("Reassignment offer {} marked as ATTENDED after check-in of appointment {}",
                            offer.getId(), event.appointmentId());
                });
    }
}
