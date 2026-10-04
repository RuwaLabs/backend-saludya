package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource returned after a successful check-in (the digital ticket).
 */
@Schema(name = "CheckInResultResource", description = "Digital ticket with the queue position")
public record CheckInResultResource(
        Long checkInId,
        Long appointmentId,
        Long queueEntryId,
        int position,
        int totalInQueue,
        String status) {
}
