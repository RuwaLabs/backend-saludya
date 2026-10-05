package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response resource for a time slot.
 */
public record TimeSlotResource(
        Long id,
        Long doctorId,
        LocalDate date,
        LocalTime startHour,
        LocalTime endHour,
        String room,
        int maxCapacity,
        int currentBookings,
        String status) {
}
