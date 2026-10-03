package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for booking a new appointment.
 */
public record BookAppointmentResource(
        @NotNull(message = "patientId must not be null")
        @Positive(message = "patientId must be positive")
        Long patientId,

        @NotNull(message = "timeSlotId must not be null")
        @Positive(message = "timeSlotId must be positive")
        Long timeSlotId) {
}
