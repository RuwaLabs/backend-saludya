package com.ruwalabs.saludya.appointments.application.queries;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Query to fetch time slots of a doctor on a given date.
 */
public record GetTimeSlotsByDoctorAndDateQuery(Long doctorId, LocalDate date) {

    public GetTimeSlotsByDoctorAndDateQuery {
        Objects.requireNonNull(doctorId, "doctorId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
    }
}
