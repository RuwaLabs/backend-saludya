package com.ruwalabs.saludya.appointments.domain.model.exceptions;

/**
 * Raised when a booking is attempted on a time slot that has been cancelled.
 */
public class TimeSlotCancelledException extends AppointmentsDomainException {

    public TimeSlotCancelledException(Long timeSlotId) {
        super("Time slot %s is cancelled".formatted(timeSlotId));
    }
}
