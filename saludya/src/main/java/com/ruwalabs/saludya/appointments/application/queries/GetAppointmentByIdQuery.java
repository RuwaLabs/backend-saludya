package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch an appointment by its id.
 */
public record GetAppointmentByIdQuery(Long id) {

    public GetAppointmentByIdQuery {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
