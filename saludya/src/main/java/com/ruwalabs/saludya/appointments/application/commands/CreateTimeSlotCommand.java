package com.ruwalabs.saludya.appointments.application.commands;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Command to create a new time slot for a doctor.
 */
public record CreateTimeSlotCommand(
        Long doctorId,
        LocalDate date,
        LocalTime startHour,
        LocalTime endHour,
        int maxCapacity) {

    public CreateTimeSlotCommand {
        Objects.requireNonNull(doctorId, "doctorId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(startHour, "startHour cannot be null");
        Objects.requireNonNull(endHour, "endHour cannot be null");
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive");
        }
    }
}
