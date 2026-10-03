package com.ruwalabs.saludya.appointments.interfaces.rest.resources;

/**
 * Response resource for a medical specialty.
 */
public record SpecialtyResource(
        Long id,
        String name,
        String description) {
}
