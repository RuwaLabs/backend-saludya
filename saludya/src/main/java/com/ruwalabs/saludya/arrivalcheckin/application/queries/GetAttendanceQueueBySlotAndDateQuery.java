package com.ruwalabs.saludya.arrivalcheckin.application.queries;

import java.time.LocalDate;

/**
 * Query to resolve the attendance queue of a time slot on a given date.
 *
 * @param timeSlotId the time slot identifier
 * @param date       the queue date
 */
public record GetAttendanceQueueBySlotAndDateQuery(Long timeSlotId, LocalDate date) {

    public GetAttendanceQueueBySlotAndDateQuery {
        if (timeSlotId == null) {
            throw new IllegalArgumentException("timeSlotId must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
    }
}
