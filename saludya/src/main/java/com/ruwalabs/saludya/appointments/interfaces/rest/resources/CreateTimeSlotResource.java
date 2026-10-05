package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request payload for creating a new time slot.
 */
public record CreateTimeSlotResource(
        @NotNull(message = "doctorId must not be null")
        @Positive(message = "doctorId must be positive")
        Long doctorId,

        @NotNull(message = "date must not be null")
        LocalDate date,

        @NotNull(message = "startHour must not be null")
        LocalTime startHour,

        @NotNull(message = "endHour must not be null")
        LocalTime endHour,

        String room,

        @Positive(message = "maxCapacity must be positive")
        int maxCapacity) {
}
