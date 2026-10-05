package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.AppointmentResource;

/**
 * Converts an {@link Appointment} aggregate into an {@link AppointmentResource}.
 */
public final class AppointmentResourceFromEntityAssembler {

    private AppointmentResourceFromEntityAssembler() {
    }

    public static AppointmentResource toResourceFromEntity(Appointment entity) {
        return new AppointmentResource(
                entity.getId(),
                entity.getTimeSlotId(),
                entity.getPatientId(),
                entity.getBookingOrder().value(),
                entity.getBookingCode(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
