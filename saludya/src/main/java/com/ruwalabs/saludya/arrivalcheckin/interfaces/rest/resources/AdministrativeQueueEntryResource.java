package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources;

import java.time.Instant;

/** Queue entry enriched with the patient and reservation details needed by staff. */
public record AdministrativeQueueEntryResource(
        Long id,
        Long attendanceQueueId,
        Long checkInId,
        Long appointmentId,
        String bookingCode,
        Long patientId,
        String patientName,
        int position,
        String status,
        Instant calledAt,
        Instant attendedAt) {
}
