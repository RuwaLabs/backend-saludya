package com.ruwalabs.saludya.appointments.application.commandservices;

import com.ruwalabs.saludya.appointments.application.commands.CreateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCapacityCommand;
import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;

/**
 * Command service for time slot use cases.
 */
public interface TimeSlotCommandService {

    Result<TimeSlot, ApplicationError> create(CreateTimeSlotCommand command);

    Result<TimeSlot, ApplicationError> updateCapacity(UpdateTimeSlotCapacityCommand command);

    Result<TimeSlot, ApplicationError> update(UpdateTimeSlotCommand command);
}
