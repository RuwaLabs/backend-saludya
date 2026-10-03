package com.ruwalabs.saludya.appointments.domain.model.exceptions;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;

/**
 * Raised when a state transition is requested on an appointment that is no
 * longer active (already cancelled, absent, attended or expired).
 */
public class AppointmentNotActiveException extends AppointmentsDomainException {

    public AppointmentNotActiveException(Long appointmentId, AppointmentStatus status) {
        super("Appointment %s cannot change state from %s because it is not active"
                .formatted(appointmentId, status));
    }
}
