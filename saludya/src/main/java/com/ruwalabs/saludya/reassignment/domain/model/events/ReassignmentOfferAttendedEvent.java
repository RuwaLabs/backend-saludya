package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when an accepted candidate arrives and is attended.
 *
 * @param offerId       the identifier of the {@code ReassignmentOffer}
 * @param appointmentId the identifier of the candidate patient's appointment
 * @param attendedAt    the instant at which the candidate was attended
 */
public record ReassignmentOfferAttendedEvent(
        Long offerId,
        Long appointmentId,
        Instant attendedAt) {
}
