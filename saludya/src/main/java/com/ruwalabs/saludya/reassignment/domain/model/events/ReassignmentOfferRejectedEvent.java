package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when a candidate rejects a reassignment offer.
 *
 * <p>Carries the freed slot and the replaced appointment so the reassignment chain can
 * offer the same slot to the next candidate in the booking queue.</p>
 *
 * @param offerId               the identifier of the rejected {@code ReassignmentOffer}
 * @param appointmentId         the identifier of the candidate patient's appointment
 * @param freedTimeSlotId       the identifier of the freed time slot still available
 * @param originalAppointmentId the identifier of the appointment that freed the slot
 * @param rejectedAt            the instant at which the offer was rejected
 */
public record ReassignmentOfferRejectedEvent(
        Long offerId,
        Long appointmentId,
        Long freedTimeSlotId,
        Long originalAppointmentId,
        Instant rejectedAt) {
}
