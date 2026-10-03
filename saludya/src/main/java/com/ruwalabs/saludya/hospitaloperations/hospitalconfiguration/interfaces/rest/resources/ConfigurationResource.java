package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources;

import java.time.LocalDateTime;

/**
 * REST representation of the hospital configuration.
 */
public record ConfigurationResource(
        Long id,
        int maxCapacityPerSlot,
        String bookingOrderScope,
        int checkInToleranceMinutes,
        int postCallToleranceMinutes,
        int reassignmentResponseTimeoutMin,
        String bookingCutoffTime,
        int cancellationDeadlineHours,
        boolean attendanceQueueVisible,
        LocalDateTime updatedAt
) {
}