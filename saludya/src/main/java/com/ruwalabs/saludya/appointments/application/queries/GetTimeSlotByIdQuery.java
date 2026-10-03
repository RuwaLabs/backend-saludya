package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch a time slot by its id.
 */
public record GetTimeSlotByIdQuery(Long id) {

    public GetTimeSlotByIdQuery {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }
}
