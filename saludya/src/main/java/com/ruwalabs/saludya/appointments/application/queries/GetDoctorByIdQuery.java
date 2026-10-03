package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch a doctor by its id.
 */
public record GetDoctorByIdQuery(Long id) {

    public GetDoctorByIdQuery {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
