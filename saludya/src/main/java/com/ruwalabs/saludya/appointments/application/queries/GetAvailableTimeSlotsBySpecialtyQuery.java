package com.ruwalabs.saludya.appointments.application.queries;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Query to fetch available time slots of a specialty on a given date.
 */
public record GetAvailableTimeSlotsBySpecialtyQuery(Long specialtyId, LocalDate date) {

    public GetAvailableTimeSlotsBySpecialtyQuery {
        Objects.requireNonNull(specialtyId, "specialtyId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
    }
}
