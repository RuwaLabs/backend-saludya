package com.ruwalabs.saludya.reassignment.domain.model.events;

import java.time.Instant;

/**
 * Domain event raised when a reassignment offer is sent to a candidate patient.
 *
 * <p>Published after the offer has been persisted (so {@code offerId} is available)
 * and carries the freed time slot being offered as well as the timestamp of the offer.</p>
 *
 * @param offerId          the identifier of the persisted {@code ReassignmentOffer}
 * @param appointmentId    the identifier of the candidate patient's appointment
 * @param freedTimeSlotId  the identifier of the freed time slot being offered
 * @param offeredAt        the instant at which the offer was sent
 */
public record ReassignmentOfferSentEvent(
        Long offerId,
        Long appointmentId,
        Long freedTimeSlotId,
        Instant offeredAt) {
}
