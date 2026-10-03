package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.commands;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.enums.BookingOrderScope;

import java.time.LocalTime;

/**
 * Command used to update the hospital configuration.
 */
public record UpdateConfigurationCommand(
        int maxCapacityPerSlot,
        BookingOrderScope bookingOrderScope,
        int checkInToleranceMinutes,
        int postCallToleranceMinutes,
        int reassignmentResponseTimeoutMin,
        LocalTime bookingCutoffTime,
        int cancellationDeadlineHours,
        boolean attendanceQueueVisible
) {
}