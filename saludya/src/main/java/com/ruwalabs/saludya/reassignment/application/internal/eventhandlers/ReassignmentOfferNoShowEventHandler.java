package com.ruwalabs.saludya.reassignment.application.internal.eventhandlers;

import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferNoShowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Application-layer event handler for {@link ReassignmentOfferNoShowEvent}.
 *
 * <p>When an accepted candidate does not arrive within the window, their appointment is
 * marked absent and the freed slot is closed (no further offer is made for it).</p>
 */
@Service
@Slf4j
public class ReassignmentOfferNoShowEventHandler {

    private final AppointmentLookupService appointmentLookupService;

    public ReassignmentOfferNoShowEventHandler(AppointmentLookupService appointmentLookupService) {
        this.appointmentLookupService = appointmentLookupService;
    }

    @EventListener
    public void on(ReassignmentOfferNoShowEvent event) {
        log.info("Offer {} ended in no-show; marking appointment {} as absent and closing slot {}",
                event.offerId(), event.appointmentId(), event.freedTimeSlotId());
        if (event.appointmentId() != null) {
            appointmentLookupService.markAbsent(event.appointmentId());
        }
    }
}
