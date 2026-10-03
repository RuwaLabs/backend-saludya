package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when a candidate accepts a reassignment offer.
 *
 * <p>Carries the full snapshot the reassignment chain needs to continue, plus the
 * information the {@code Appointments & Booking} context needs to move the candidate's
 * appointment to the freed slot and transfer the {@code bookingOrder} of the replaced
 * (no-show) appointment.</p>
 *
 * @param offerId               the identifier of the accepted {@code ReassignmentOffer}
 * @param appointmentId         the identifier of the candidate patient's appointment
 * @param freedTimeSlotId       the identifier of the freed time slot being taken
 * @param originalAppointmentId the identifier of the appointment that freed the slot
 * @param candidateTimeSlotId   the identifier of the candidate's original slot (now freed)
 * @param acceptedAt            the instant at which the offer was accepted
 */
public record ReassignmentOfferAcceptedEvent(
        Long offerId,
        Long appointmentId,
        Long freedTimeSlotId,
        Long originalAppointmentId,
        Long candidateTimeSlotId,
        Instant acceptedAt) {
}
