package com.ruwalabs.saludya.appointments.domain.model.exceptions;

/**
 * Raised when a booking is attempted on a time slot that has already reached
 * its maximum capacity.
 */
public class TimeSlotFullException extends AppointmentsDomainException {

    public TimeSlotFullException(Long timeSlotId) {
        super("Time slot %s has reached its maximum capacity".formatted(timeSlotId));
    }
}
