package com.ruwalabs.saludya.appointments.domain.model.valueobjects;

/**
 * Lifecycle states of a medical appointment.
 *
 * <p>{@code RESERVED} is the initial state assigned at booking time.
 * The remaining states are reached through the transition methods of the
 * {@code Appointment} aggregate root.</p>
 */
public enum AppointmentStatus {
    RESERVED,
    CONFIRMED,
    CANCELLED,
    ABSENT,
    ATTENDED,
    EXPIRED
}
