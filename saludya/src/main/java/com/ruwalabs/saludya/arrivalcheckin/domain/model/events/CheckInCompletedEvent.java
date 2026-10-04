package com.ruwalabs.saludya.arrivalcheckin.domain.model.events;

import java.time.Instant;

/**
 * Domain event published when a patient completes the check-in and enters the
 * attendance queue.
 *
 * @param checkInId         the check-in identifier
 * @param appointmentId     the appointment identifier
 * @param attendanceQueueId the attendance queue identifier
 * @param position          the assigned position in the queue
 * @param completedAt       the instant the check-in was completed
 */
public record CheckInCompletedEvent(
        Long checkInId,
        Long appointmentId,
        Long attendanceQueueId,
        int position,
        Instant completedAt) {
}
