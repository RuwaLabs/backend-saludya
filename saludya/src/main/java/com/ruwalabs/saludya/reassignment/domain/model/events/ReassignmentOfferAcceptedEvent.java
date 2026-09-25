package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when a patient accepts a reassignment offer.
 *
 * @param offerId       the identifier of the accepted {@code ReassignmentOffer}
 * @param appointmentId the identifier of the candidate patient's appointment
 * @param acceptedAt    the instant at which the offer was accepted
 */
public record ReassignmentOfferAcceptedEvent(
        Long offerId,
        Long appointmentId,
        Instant acceptedAt) {
}
