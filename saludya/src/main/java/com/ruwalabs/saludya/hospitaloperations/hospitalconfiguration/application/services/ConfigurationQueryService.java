package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GenerateReportQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GetDashboardQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service responsible for configuration queries.
 */
public interface ConfigurationQueryService {

    HospitalConfiguration getConfiguration();

    ReportResult generateReport(
            GenerateReportQuery query
    );

    DashboardResult getDashboard(
            GetDashboardQuery query
    );

    record ReportResult(
            String format,
            String generatedAt,
            String content
    ) {
    }

    record DashboardResult(
            Long configurationId,
            int maxCapacityPerSlot,
            String bookingOrderScope,
            int checkInToleranceMinutes,
            int postCallToleranceMinutes,
            int reassignmentResponseTimeoutMin,
            String bookingCutoffTime,
            int cancellationDeadlineHours,
            boolean attendanceQueueVisible,
            List<DashboardMetric> metrics,
            boolean externalDataAvailable
    ) {
    }

    record DashboardMetric(
            String name,
            Long value
    ) {
    }
}