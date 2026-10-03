package com.ruwalabs.saludya.appointments.interfaces.rest.transform;

import com.ruwalabs.saludya.appointments.application.commands.BookAppointmentCommand;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.BookAppointmentResource;

/**
 * Converts a {@link BookAppointmentResource} into a {@link BookAppointmentCommand}.
 */
public final class BookAppointmentCommandFromResourceAssembler {

    private BookAppointmentCommandFromResourceAssembler() {
    }

    public static BookAppointmentCommand toCommandFromResource(BookAppointmentResource resource) {
        return new BookAppointmentCommand(resource.patientId(), resource.timeSlotId());
    }
}
