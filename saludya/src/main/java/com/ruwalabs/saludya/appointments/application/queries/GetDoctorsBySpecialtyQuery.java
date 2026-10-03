package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch doctors of a specialty.
 */
public record GetDoctorsBySpecialtyQuery(Long specialtyId) {

    public GetDoctorsBySpecialtyQuery {
        if (specialtyId == null || specialtyId <= 0) {
            throw new IllegalArgumentException("specialtyId must be positive");
        }
    }
}
