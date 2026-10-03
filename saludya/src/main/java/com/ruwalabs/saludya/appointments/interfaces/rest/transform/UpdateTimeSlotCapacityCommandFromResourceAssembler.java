package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCapacityCommand;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.UpdateTimeSlotCapacityResource;

/**
 * Converts a {@link UpdateTimeSlotCapacityResource} into a
 * {@link UpdateTimeSlotCapacityCommand}.
 */
public final class UpdateTimeSlotCapacityCommandFromResourceAssembler {

    private UpdateTimeSlotCapacityCommandFromResourceAssembler() {
    }

    public static UpdateTimeSlotCapacityCommand toCommandFromResource(
            Long timeSlotId,
            UpdateTimeSlotCapacityResource resource) {
        return new UpdateTimeSlotCapacityCommand(timeSlotId, resource.maxCapacity());
    }
}
