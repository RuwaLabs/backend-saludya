package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.enums.BookingOrderScope;

import java.time.LocalTime;

/**
 * REST request used to update hospital configuration.
 */
public record UpdateConfigurationResource(
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