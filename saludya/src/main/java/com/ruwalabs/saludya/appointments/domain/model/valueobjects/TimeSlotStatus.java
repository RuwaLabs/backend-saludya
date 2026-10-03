package com.ruwalabs.saludya.appointments.domain.model.valueobjects;

/**
 * Availability states of a time slot.
 *
 * <p>{@code AVAILABLE} is the initial state when a slot is created.
 * {@code FULL} is reached when {@code currentBookings} reaches
 * {@code maxCapacity}, and {@code CANCELLED} when the slot is closed
 * by the admission staff.</p>
 */
public enum TimeSlotStatus {
    AVAILABLE,
    FULL,
    CANCELLED
}
