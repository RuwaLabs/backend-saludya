package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when a patient rejects a reassignment offer.
 *
 * @param offerId       the identifier of the rejected {@code ReassignmentOffer}
 * @param appointmentId the identifier of the candidate patient's appointment
 * @param rejectedAt    the instant at which the offer was rejected
 */
public record ReassignmentOfferRejectedEvent(
        Long offerId,
        Long appointmentId,
        Instant rejectedAt) {
}
