package com.ruwalabs.saludya.appointments.application.internal.commandservices;

import com.ruwalabs.saludya.appointments.application.commands.CreateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCapacityCommand;
import com.ruwalabs.saludya.appointments.application.commands.UpdateTimeSlotCommand;
import com.ruwalabs.saludya.appointments.application.commandservices.TimeSlotCommandService;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.TimeSlotStatus;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Objects;

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
                    command.room(),
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

    @Override
    @Transactional
    public Result<TimeSlot, ApplicationError> update(UpdateTimeSlotCommand command) {
        var timeSlot = timeSlotRepository.findById(command.timeSlotId()).orElse(null);
        if (timeSlot == null) {
            return Result.failure(ApplicationError.notFound("TimeSlot", command.timeSlotId().toString()));
        }

        var newDoctorId = command.doctorId() != null ? command.doctorId() : timeSlot.getDoctorId();
        var newStartHour = command.startHour() != null ? command.startHour() : timeSlot.getStartHour();
        var newEndHour = command.endHour() != null ? command.endHour() : timeSlot.getEndHour();
        TimeSlotStatus newStatus = timeSlot.getStatus();
        if (command.status() != null && !command.status().isBlank()) {
            try {
                newStatus = TimeSlotStatus.valueOf(command.status().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return Result.failure(ApplicationError.validationError(
                        "status", "Unknown time slot status: " + command.status()));
            }
        }

        boolean scheduleChanged = !Objects.equals(newDoctorId, timeSlot.getDoctorId())
                || !Objects.equals(newStartHour, timeSlot.getStartHour())
                || !Objects.equals(newEndHour, timeSlot.getEndHour());
        if (scheduleChanged && timeSlot.hasBookings()) {
            return Result.failure(ApplicationError.conflict(
                    "TimeSlot", "The slot has confirmed bookings and cannot be rescheduled"));
        }
        if (scheduleChanged && overlapsAnotherSlot(timeSlot, newDoctorId, newStartHour, newEndHour)) {
            return Result.failure(ApplicationError.conflict(
                    "TimeSlot", "The doctor already has an overlapping slot on that date"));
        }

        try {
            timeSlot.updateDetails(newDoctorId, newStartHour, newEndHour, newStatus);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("TimeSlot", ex.getMessage()));
        }
        return Result.success(timeSlotRepository.save(timeSlot));
    }

    private boolean overlapsAnotherSlot(
            TimeSlot current,
            Long doctorId,
            LocalTime startHour,
            LocalTime endHour) {
        return timeSlotRepository.findByDoctorAndDate(doctorId, current.getDate()).stream()
                .filter(other -> !Objects.equals(other.getId(), current.getId()))
                .anyMatch(other -> other.getStatus() != TimeSlotStatus.CANCELLED
                        && startHour.isBefore(other.getEndHour())
                        && other.getStartHour().isBefore(endHour));
    }
}
