package com.ruwalabs.saludya.reassignment.application.commands;

import java.util.Objects;

/**
 * Command to trigger the reassignment of a freed time slot.
 *
 * <p>Sent when a time slot becomes free because its patient was declared absent. The
 * application layer finds the next candidate by {@code bookingOrder} and creates a
 * {@code PENDING} offer.</p>
 *
 * @param originalAppointmentId the appointment that freed the slot (the no-show)
 * @param freedTimeSlotId       the time slot that became available
 */
public record SendReassignmentOfferCommand(
        Long originalAppointmentId,
        Long freedTimeSlotId) {

    public SendReassignmentOfferCommand {
        Objects.requireNonNull(originalAppointmentId, "originalAppointmentId must not be null");
        Objects.requireNonNull(freedTimeSlotId, "freedTimeSlotId must not be null");
    }
}
