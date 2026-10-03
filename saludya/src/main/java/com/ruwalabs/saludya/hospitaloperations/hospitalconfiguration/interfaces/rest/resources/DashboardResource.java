package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources;

import java.util.List;

/**
 * REST representation of the operational dashboard.
 */
public record DashboardResource(
        Long configurationId,
        int maxCapacityPerSlot,
        String bookingOrderScope,
        int checkInToleranceMinutes,
        int postCallToleranceMinutes,
        int reassignmentResponseTimeoutMin,
        String bookingCutoffTime,
        int cancellationDeadlineHours,
        boolean attendanceQueueVisible,
        List<DashboardMetricResource> metrics,
        boolean externalDataAvailable
) {

    public record DashboardMetricResource(
            String name,
            Long value
    ) {
    }
}