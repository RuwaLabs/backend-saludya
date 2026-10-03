package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferNoShowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link ReassignmentOfferNoShowEvent}.
 *
 * <p>When an accepted candidate does not arrive within the window, the freed slot is
 * closed and no further offer is made for it. The {@code Appointments & Booking} context
 * (which consumes this event) is responsible for marking the candidate's appointment as
 * absent, so they lose their slot for that day/specialty.</p>
 */
@Service
@Slf4j
public class ReassignmentOfferNoShowEventHandler {

    /**
     * Reacts to {@link ReassignmentOfferNoShowEvent} by closing the freed slot.
     *
     * <p>No chain re-trigger happens here: the slot is simply left closed.</p>
     *
     * @param event the offer-no-show event
     */
    @EventListener
    public void on(ReassignmentOfferNoShowEvent event) {
        log.info("Offer {} ended in no-show; closing freed slot {}",
                event.offerId(), event.freedTimeSlotId());
    }
}
