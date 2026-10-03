package com.ruwalabs.saludya.appointments.domain.model.valueobjects;

/**
 * Sequential booking position of an appointment.
 *
 * <p>The value is unique per specialty, date and establishment. Its scope is
 * enforced at calculation time by
 * {@code AppointmentRepository#getNextBookingOrderBySpecialty}, so the value
 * object only carries the assigned sequence number. It is immutable once
 * assigned.</p>
 */
public record BookingOrder(int value) {

    public BookingOrder {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    "bookingOrder value must be positive");
        }
    }
}
