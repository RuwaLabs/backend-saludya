package com.ruwalabs.saludya.arrivalcheckin.domain.model.events;

import java.time.Instant;

/**
 * Domain event published when a patient is called to the consultation room.
 *
 * @param queueEntryId      the queue entry identifier
 * @param attendanceQueueId the attendance queue identifier
 * @param position          the patient's position in the queue
 * @param calledAt          the instant the patient was called
 */
public record PatientCalledEvent(
        Long queueEntryId,
        Long attendanceQueueId,
        int position,
        Instant calledAt) {
}
