package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

import java.time.Instant;

/**
 * Minimal appointment data needed by the Arrival context.
 *
 * @param appointmentId the appointment identifier
 * @param timeSlotId    the time slot identifier
 * @param patientId     the patient profile identifier
 * @param status        the appointment status
 * @param slotStart     the instant the time slot starts
 * @param bookingCode   the unique reservation code
 * @param specialtyName the specialty name
 * @param doctorName    the assigned professional's full name
 * @param room          the consultation room
 */
public record AppointmentInfo(
        Long appointmentId,
        Long timeSlotId,
        Long patientId,
        String status,
        Instant slotStart,
        String bookingCode,
        String specialtyName,
        String doctorName,
        String room) {
}
