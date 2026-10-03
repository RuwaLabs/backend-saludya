package com.ruwalabs.saludya.appointments.application.commandservices;

import com.ruwalabs.saludya.appointments.application.commands.BookAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commands.CancelAppointmentCommand;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;

/**
 * Command service for appointment use cases.
 */
public interface AppointmentCommandService {

    Result<Appointment, ApplicationError> book(BookAppointmentCommand command);

    Result<Appointment, ApplicationError> cancel(CancelAppointmentCommand command);
}
