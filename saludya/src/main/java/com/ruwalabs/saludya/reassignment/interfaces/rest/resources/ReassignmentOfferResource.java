package com.ruwalabs.saludya.reassignment.interfaces.rest.resources;

import java.time.Instant;

/**
 * REST resource representing a reassignment offer.
 *
 * @param id                   the offer identifier
 * @param appointmentId        the candidate patient's appointment identifier
 * @param originalAppointmentId the appointment that freed the slot
 * @param freedTimeSlotId      the freed time slot being offered
 * @param candidateTimeSlotId  the candidate's current time slot
 * @param status               the offer status
 * @param offeredAt            the instant the offer was sent
 * @param respondedAt          the instant the candidate responded (nullable)
 * @param expiresAt            the instant the offer window closes
 */
public record ReassignmentOfferResource(
        Long id,
        Long appointmentId,
        Long originalAppointmentId,
        Long freedTimeSlotId,
        Long candidateTimeSlotId,
        String status,
        Instant offeredAt,
        Instant respondedAt,
        Instant expiresAt) {
}
