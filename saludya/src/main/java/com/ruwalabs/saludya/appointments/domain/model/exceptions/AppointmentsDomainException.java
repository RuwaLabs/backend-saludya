package com.ruwalabs.saludya.appointments.domain.model.exceptions;

/**
 * Base class for domain-level exceptions raised by the Appointments &amp; Booking
 * bounded context.
 */
public abstract class AppointmentsDomainException extends RuntimeException {

    protected AppointmentsDomainException(String message) {
        super(message);
    }
}
