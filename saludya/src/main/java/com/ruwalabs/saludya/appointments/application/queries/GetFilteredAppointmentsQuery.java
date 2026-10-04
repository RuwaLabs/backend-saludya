package com.ruwalabs.saludya.appointments.application.queries;

import java.time.LocalDate;

/**
 * Query to list appointments by optional filters (patient, slot, doctor, specialty,
 * date and status). All fields may be {@code null}.
 *
 * @param patientId   optional patient filter
 * @param timeSlotId  optional time slot filter
 * @param doctorId    optional doctor filter
 * @param specialtyId optional specialty filter
 * @param date        optional date filter
 * @param status      optional status filter (name of {@code AppointmentStatus})
 */
public record GetFilteredAppointmentsQuery(
        Long patientId,
        Long timeSlotId,
        Long doctorId,
        Long specialtyId,
        LocalDate date,
        String status) {
}
