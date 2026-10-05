package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Resource representing the digital attention ticket of a checked-in patient.
 */
@Schema(name = "CheckInResource", description = "Digital ticket data: turn code, specialty, professional, room and check-in details")
public record CheckInResource(
        Long id,
        Long appointmentId,
        String bookingCode,
        String turnCode,
        String specialtyName,
        String doctorName,
        String room,
        Instant scheduledAt,
        String status,
        Instant checkedInAt) {
}
