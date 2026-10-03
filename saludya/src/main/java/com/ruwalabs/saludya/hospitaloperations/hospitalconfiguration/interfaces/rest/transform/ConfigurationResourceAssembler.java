package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.transform;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services.ConfigurationQueryService;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.ConfigurationResource;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.DashboardResource;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.ReportResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Transforms application/domain models into REST resources.
 */
@Component
public class ConfigurationResourceAssembler {

    public ConfigurationResource toResource(
            HospitalConfiguration configuration
    ) {
        return new ConfigurationResource(
                configuration.getId(),
                configuration.getMaxCapacityPerSlot(),
                configuration.getBookingOrderScope().name(),
                configuration.getCheckInToleranceMinutes(),
                configuration.getPostCallToleranceMinutes(),
                configuration.getReassignmentResponseTimeoutMin(),
                configuration.getBookingCutoffTime().toString(),
                configuration.getCancellationDeadlineHours(),
                configuration.isAttendanceQueueVisible(),
                configuration.getUpdatedAt()
        );
    }

    public DashboardResource toResource(
            ConfigurationQueryService.DashboardResult dashboard
    ) {
        List<DashboardResource.DashboardMetricResource> metrics =
                dashboard.metrics()
                        .stream()
                        .map(metric ->
                                new DashboardResource.DashboardMetricResource(
                                        metric.name(),
                                        metric.value()
                                )
                        )
                        .toList();

        return new DashboardResource(
                dashboard.configurationId(),
                dashboard.maxCapacityPerSlot(),
                dashboard.bookingOrderScope(),
                dashboard.checkInToleranceMinutes(),
                dashboard.postCallToleranceMinutes(),
                dashboard.reassignmentResponseTimeoutMin(),
                dashboard.bookingCutoffTime(),
                dashboard.cancellationDeadlineHours(),
                dashboard.attendanceQueueVisible(),
                metrics,
                dashboard.externalDataAvailable()
        );
    }

    public ReportResource toResource(
            ConfigurationQueryService.ReportResult report
    ) {
        return new ReportResource(
                report.format(),
                report.generatedAt(),
                report.content()
        );
    }
}