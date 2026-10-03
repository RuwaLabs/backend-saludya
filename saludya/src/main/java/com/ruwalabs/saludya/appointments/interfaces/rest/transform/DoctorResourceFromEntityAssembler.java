package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.DoctorResource;

/**
 * Converts a {@link Doctor} entity into a {@link DoctorResource}.
 */
public final class DoctorResourceFromEntityAssembler {

    private DoctorResourceFromEntityAssembler() {
    }

    public static DoctorResource toResourceFromEntity(Doctor entity) {
        return new DoctorResource(
                entity.getId(),
                entity.getSpecialtyId(),
                entity.getName(),
                entity.getLastname());
    }
}
