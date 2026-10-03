package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when an accepted candidate fails to arrive within the window.
 *
 * <p>Carries the information the {@code Appointments & Booking} context needs to mark
 * the candidate's appointment as absent (they lose their slot for that day/specialty).</p>
 *
 * @param offerId         the identifier of the {@code ReassignmentOffer}
 * @param appointmentId   the identifier of the candidate patient's appointment
 * @param freedTimeSlotId the identifier of the freed time slot that was not taken
 * @param noShowAt        the instant at which the no-show was declared
 */
public record ReassignmentOfferNoShowEvent(
        Long offerId,
        Long appointmentId,
        Long freedTimeSlotId,
        Instant noShowAt) {
}
