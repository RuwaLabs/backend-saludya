package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Resource representing a check-in.
 */
@Schema(name = "CheckInResource", description = "Check-in data")
public record CheckInResource(
        Long id,
        Long appointmentId,
        String status,
        Instant checkedInAt) {
}
