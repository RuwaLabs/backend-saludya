package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.UpdateTimeSlotResource;

/**
 * Converts an {@link UpdateTimeSlotResource} into an {@link UpdateTimeSlotCommand}.
 */
public final class UpdateTimeSlotCommandFromResourceAssembler {

    private UpdateTimeSlotCommandFromResourceAssembler() {
    }

    public static UpdateTimeSlotCommand toCommandFromResource(Long timeSlotId, UpdateTimeSlotResource resource) {
        return new UpdateTimeSlotCommand(
                timeSlotId,
                resource.doctorId(),
                resource.startHour(),
                resource.endHour(),
                resource.status());
    }
}
