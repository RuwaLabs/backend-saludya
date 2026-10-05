package com.ruwalabs.saludya.appointments.application.commands;

import java.time.LocalTime;

/**
 * Command to update the editable attributes of a time slot: assigned doctor,
 * schedule and status.
 */
public record UpdateTimeSlotCommand(
        Long timeSlotId,
        Long doctorId,
        LocalTime startHour,
        LocalTime endHour,
        String status) {

    public UpdateTimeSlotCommand {
        if (timeSlotId == null || timeSlotId <= 0) {
            throw new IllegalArgumentException("timeSlotId must be positive");
        }
    }
}
