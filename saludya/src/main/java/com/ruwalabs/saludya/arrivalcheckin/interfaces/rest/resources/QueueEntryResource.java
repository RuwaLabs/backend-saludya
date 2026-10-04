package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Resource representing a queue entry.
 */
@Schema(name = "QueueEntryResource", description = "Attendance queue entry data")
public record QueueEntryResource(
        Long id,
        Long attendanceQueueId,
        Long checkInId,
        int position,
        String status,
        Instant calledAt,
        Instant attendedAt) {
}
