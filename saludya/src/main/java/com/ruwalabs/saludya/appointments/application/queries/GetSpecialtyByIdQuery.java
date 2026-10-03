package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch a specialty by its id.
 */
public record GetSpecialtyByIdQuery(Long id) {

    public GetSpecialtyByIdQuery {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
