package com.ruwalabs.saludya.appointments.application.commands;

/**
 * Command to update the maximum capacity of a time slot.
 */
public record UpdateTimeSlotCapacityCommand(Long timeSlotId, int maxCapacity) {

    public UpdateTimeSlotCapacityCommand {
        if (timeSlotId == null || timeSlotId <= 0) {
            throw new IllegalArgumentException("timeSlotId must be positive");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive");
        }
    }
}
