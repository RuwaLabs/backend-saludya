package com.ruwalabs.saludya.appointments.application.commands;

/**
 * Command to book a new appointment for a patient in a specific time slot.
 */
public record BookAppointmentCommand(Long patientId, Long timeSlotId) {

    public BookAppointmentCommand {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId must be positive");
        }
        if (timeSlotId == null || timeSlotId <= 0) {
            throw new IllegalArgumentException("timeSlotId must be positive");
        }
    }
}
