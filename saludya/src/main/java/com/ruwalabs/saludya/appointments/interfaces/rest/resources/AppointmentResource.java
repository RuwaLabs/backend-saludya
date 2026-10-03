package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

import java.time.Instant;

/**
 * Response resource for an appointment.
 */
public record AppointmentResource(
        Long id,
        Long timeSlotId,
        Long patientId,
        int bookingOrder,
        String status,
        Instant createdAt,
        Instant updatedAt) {
}
