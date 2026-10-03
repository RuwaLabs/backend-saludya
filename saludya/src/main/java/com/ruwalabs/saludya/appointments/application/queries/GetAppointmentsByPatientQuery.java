package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch all appointments of a patient.
 */
public record GetAppointmentsByPatientQuery(Long patientId) {

    public GetAppointmentsByPatientQuery {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId must be positive");
        }
    }
}
