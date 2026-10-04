package com.ruwalabs.saludya.arrivalcheckin.application.queries;

/**
 * Query to get the check-in of an appointment.
 *
 * @param appointmentId the appointment identifier
 */
public record GetCheckInByAppointmentQuery(Long appointmentId) {
}
