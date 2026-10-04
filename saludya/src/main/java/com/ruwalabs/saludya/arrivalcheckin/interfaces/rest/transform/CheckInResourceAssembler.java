package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.CheckInResource;

/**
 * Assembler from {@link CheckIn} to {@link CheckInResource}.
 */
public final class CheckInResourceAssembler {

    private CheckInResourceAssembler() {
    }

    public static CheckInResource toResource(CheckIn checkIn) {
        return new CheckInResource(
                checkIn.getId(),
                checkIn.getIdAppointment(),
                checkIn.getStatus().name(),
                checkIn.getCheckedInAt());
    }
}
