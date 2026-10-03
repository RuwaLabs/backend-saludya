package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.application.commands.CreateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.CreateTimeSlotResource;

/**
 * Converts a {@link CreateTimeSlotResource} into a {@link CreateTimeSlotCommand}.
 */
public final class CreateTimeSlotCommandFromResourceAssembler {

    private CreateTimeSlotCommandFromResourceAssembler() {
    }

    public static CreateTimeSlotCommand toCommandFromResource(CreateTimeSlotResource resource) {
        return new CreateTimeSlotCommand(
                resource.doctorId(),
                resource.date(),
                resource.startHour(),
                resource.endHour(),
                resource.maxCapacity());
    }
}
