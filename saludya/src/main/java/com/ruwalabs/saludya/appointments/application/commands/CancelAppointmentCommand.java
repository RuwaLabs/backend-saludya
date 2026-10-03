package com.ruwalabs.saludya.appointments.application.commands;

/**
 * Command to cancel an existing appointment, freeing its time slot.
 */
public record CancelAppointmentCommand(Long appointmentId) {

    public CancelAppointmentCommand {
        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("appointmentId must be positive");
        }
    }
}
