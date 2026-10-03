package com.ruwalabs.saludya.appointments.domain.model.events;

import java.time.Instant;

/**
 * Domain event published when a patient does not show up, freeing its time slot.
 *
 * <p>As with cancellation, it only signals that the slot is free again; the
 * reassignment protocol is handled by the Reassignment bounded context.</p>
 */
public record AppointmentAbsentEvent(
        Long appointmentId,
        Long freedTimeSlotId,
        Instant absentAt) {
}
