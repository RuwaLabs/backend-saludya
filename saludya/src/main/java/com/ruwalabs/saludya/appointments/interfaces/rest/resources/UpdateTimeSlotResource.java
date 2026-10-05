package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

import java.time.LocalTime;

/**
 * Request payload for editing a time slot (doctor, schedule and status).
 */
public record UpdateTimeSlotResource(
        Long doctorId,
        LocalTime startHour,
        LocalTime endHour,
        String status) {
}
