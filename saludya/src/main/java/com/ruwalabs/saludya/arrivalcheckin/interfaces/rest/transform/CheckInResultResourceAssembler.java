package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform;

import com.ruwalabs.saludya.arrivalcheckin.application.model.CheckInResult;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.CheckInResultResource;

/**
 * Assembler from {@link CheckInResult} to {@link CheckInResultResource}.
 */
public final class CheckInResultResourceAssembler {

    private CheckInResultResourceAssembler() {
    }

    public static CheckInResultResource toResource(CheckInResult result) {
        return new CheckInResultResource(
                result.checkIn().getId(),
                result.checkIn().getIdAppointment(),
                result.queueEntry().getId(),
                result.position(),
                result.totalInQueue(),
                result.checkIn().getStatus().name());
    }
}
