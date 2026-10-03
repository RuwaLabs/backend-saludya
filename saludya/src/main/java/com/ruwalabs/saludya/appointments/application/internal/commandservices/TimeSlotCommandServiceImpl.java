package com.ruwalabs.saludya.appointments.application.internal.commandservices;

import com.ruwalabs.saludya.appointments.application.commands.CreateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCapacityCommand;
import com.ruwalabs.saludya.appointments.application.commandservices.TimeSlotCommandService;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link TimeSlotCommandService}.
 */
@Service
public class TimeSlotCommandServiceImpl implements TimeSlotCommandService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotCommandServiceImpl(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    @Transactional
    public Result<TimeSlot, ApplicationError> create(CreateTimeSlotCommand command) {
        try {
            var timeSlot = TimeSlot.create(
                    command.doctorId(),
                    command.date(),
                    command.startHour(),
                    command.endHour(),
                    command.maxCapacity());
            var saved = timeSlotRepository.save(timeSlot);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("TimeSlot", e.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<TimeSlot, ApplicationError> updateCapacity(UpdateTimeSlotCapacityCommand command) {
        var timeSlot = timeSlotRepository.findById(command.timeSlotId()).orElse(null);
        if (timeSlot == null) {
            return Result.failure(ApplicationError.notFound("TimeSlot", command.timeSlotId().toString()));
        }
        try {
            timeSlot.updateMaxCapacity(command.maxCapacity());
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("TimeSlot", e.getMessage()));
        }
        var saved = timeSlotRepository.save(timeSlot);
        return Result.success(saved);
    }
}
