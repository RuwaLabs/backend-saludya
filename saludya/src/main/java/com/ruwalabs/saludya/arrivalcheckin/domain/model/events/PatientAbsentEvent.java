package com.ruwalabs.saludya.arrivalcheckin.domain.model.events;

import java.time.Instant;

/**
 * Domain event published when a patient is declared absent after the tolerance
 * window expires. It is the trigger of the reassignment protocol.
 *
 * @param queueEntryId   the queue entry identifier
 * @param appointmentId  the appointment identifier
 * @param freedTimeSlotId the time slot that is freed
 * @param absentAt       the instant the absence was declared
 */
public record PatientAbsentEvent(
        Long queueEntryId,
        Long appointmentId,
        Long freedTimeSlotId,
        Instant absentAt) {
}
