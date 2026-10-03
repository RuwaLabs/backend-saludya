package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.SpecialtyResource;

/**
 * Converts a {@link Specialty} entity into a {@link SpecialtyResource}.
 */
public final class SpecialtyResourceFromEntityAssembler {

    private SpecialtyResourceFromEntityAssembler() {
    }

    public static SpecialtyResource toResourceFromEntity(Specialty entity) {
        return new SpecialtyResource(entity.getId(), entity.getName(), entity.getDescription());
    }
}
