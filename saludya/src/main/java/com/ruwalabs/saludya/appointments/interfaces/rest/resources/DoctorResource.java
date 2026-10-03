package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

/**
 * Response resource for a doctor.
 */
public record DoctorResource(
        Long id,
        Long specialtyId,
        String name,
        String lastname) {
}
