package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when a reassignment offer expires without a response.
 *
 * @param offerId       the identifier of the expired {@code ReassignmentOffer}
 * @param appointmentId the identifier of the candidate patient's appointment
 * @param expiredAt     the instant at which the offer expired
 */
public record ReassignmentOfferExpiredEvent(
        Long offerId,
        Long appointmentId,
        Instant expiredAt) {
}
