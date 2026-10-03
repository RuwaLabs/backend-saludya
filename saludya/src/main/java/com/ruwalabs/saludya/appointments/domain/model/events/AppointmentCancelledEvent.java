package com.ruwalabs.saludya.appointments.domain.model.events;

import java.time.Instant;

/**
 * Domain event published when an appointment is cancelled, freeing its time slot.
 *
 * <p>It only signals that the slot is free again; the reassignment protocol
 * (offering the slot to the next patient in booking order) is handled by the
 * Reassignment bounded context.</p>
 */
public record AppointmentCancelledEvent(
        Long appointmentId,
        Long freedTimeSlotId,
        Instant cancelledAt) {
}
