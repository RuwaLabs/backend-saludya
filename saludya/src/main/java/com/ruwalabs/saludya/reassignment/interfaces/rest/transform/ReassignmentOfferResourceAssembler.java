package com.ruwalabs.saludya.reassignment.interfaces.rest.transform;

import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.interfaces.rest.resources.ReassignmentOfferResource;

/**
 * Static assembler between the {@code ReassignmentOffer} domain aggregate and its REST
 * resource representation.
 */
public final class ReassignmentOfferResourceAssembler {

    private ReassignmentOfferResourceAssembler() {
    }

    /**
     * Maps a domain aggregate to a REST resource.
     *
     * @param offer the domain aggregate
     * @return the corresponding resource, or {@code null} if the aggregate is {@code null}
     */
    public static ReassignmentOfferResource toResource(ReassignmentOffer offer) {
        if (offer == null) {
            return null;
        }
        return new ReassignmentOfferResource(
                offer.getId(),
                offer.getAppointmentId(),
                offer.getOriginalAppointmentId(),
                offer.getFreedTimeSlotId(),
                offer.getCandidateTimeSlotId(),
                offer.getStatus().name(),
                offer.getOfferedAt(),
                offer.getRespondedAt(),
                offer.getExpiresAt());
    }
}
