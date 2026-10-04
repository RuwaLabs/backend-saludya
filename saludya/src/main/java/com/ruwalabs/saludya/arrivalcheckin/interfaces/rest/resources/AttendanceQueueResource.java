package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Resource representing an attendance queue.
 */
@Schema(name = "AttendanceQueueResource", description = "Attendance queue data")
public record AttendanceQueueResource(
        Long id,
        Long timeSlotId,
        LocalDate date,
        String status) {
}
