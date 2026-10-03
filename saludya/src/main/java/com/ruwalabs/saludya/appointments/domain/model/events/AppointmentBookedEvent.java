package com.ruwalabs.saludya.appointments.domain.model.events;

import java.time.Instant;

/**
 * Domain event published when a new appointment is reserved.
 *
 * <p>Consumed by the Reassignment and Arrival &amp; QR Check-in bounded contexts.</p>
 */
public record AppointmentBookedEvent(
        Long appointmentId,
        Long timeSlotId,
        Long patientId,
        int bookingOrder,
        Instant bookedAt) {
}
