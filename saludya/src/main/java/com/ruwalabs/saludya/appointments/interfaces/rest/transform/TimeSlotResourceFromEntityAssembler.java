package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.TimeSlotResource;

/**
 * Converts a {@link TimeSlot} aggregate into a {@link TimeSlotResource}.
 */
public final class TimeSlotResourceFromEntityAssembler {

    private TimeSlotResourceFromEntityAssembler() {
    }

    public static TimeSlotResource toResourceFromEntity(TimeSlot entity) {
        return new TimeSlotResource(
                entity.getId(),
                entity.getDoctorId(),
                entity.getDate(),
                entity.getStartHour(),
                entity.getEndHour(),
                entity.getMaxCapacity(),
                entity.getCurrentBookings(),
                entity.getStatus().name());
    }
}
