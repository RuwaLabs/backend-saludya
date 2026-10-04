package com.ruwalabs.saludya.arrivalcheckin.application.commands;

/**
 * Command to call the next patient in an attendance queue.
 *
 * @param attendanceQueueId the attendance queue identifier
 */
public record CallNextPatientCommand(Long attendanceQueueId) {
}
