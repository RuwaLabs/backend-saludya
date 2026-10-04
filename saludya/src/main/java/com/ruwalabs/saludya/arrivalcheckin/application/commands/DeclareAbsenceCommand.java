package com.ruwalabs.saludya.arrivalcheckin.application.commands;

/**
 * Command to declare a patient absent.
 *
 * @param queueEntryId    the queue entry identifier
 * @param appointmentId   the appointment identifier
 * @param freedTimeSlotId the time slot that is freed
 */
public record DeclareAbsenceCommand(
        Long queueEntryId,
        Long appointmentId,
        Long freedTimeSlotId) {
}
