package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GenerateReportQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GetDashboardQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.repositories.HospitalConfigurationRepository;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.reports.ReportGeneratorAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default implementation of configuration queries.
 */
@Service
@Transactional(readOnly = true)
public class ConfigurationQueryServiceImpl
        implements ConfigurationQueryService {

    private final HospitalConfigurationRepository repository;
    private final ReportGeneratorAdapter reportGenerator;

    public ConfigurationQueryServiceImpl(
            HospitalConfigurationRepository repository,
            ReportGeneratorAdapter reportGenerator
    ) {
        this.repository = repository;
        this.reportGenerator = reportGenerator;
    }

    @Override
    public HospitalConfiguration getConfiguration() {
        return repository.findDefault()
                .orElseThrow(() -> new IllegalStateException(
                        "Hospital configuration was not found"
                ));
    }

    @Override
    public ReportResult generateReport(
            GenerateReportQuery query
    ) {
        if (query == null) {
            throw new IllegalArgumentException(
                    "Generate report query cannot be null"
            );
        }

        HospitalConfiguration configuration = getConfiguration();

        String content = reportGenerator.generate(
                configuration,
                query
        );

        return new ReportResult(
                query.format().name(),
                LocalDateTime.now().toString(),
                content
        );
    }

    @Override
    public DashboardResult getDashboard(
            GetDashboardQuery query
    ) {
        if (query == null) {
            throw new IllegalArgumentException(
                    "Get dashboard query cannot be null"
            );
        }

        HospitalConfiguration configuration = getConfiguration();

        /*
         * Appointment, attendance and reassignment metrics belong
         * to other bounded contexts and will be integrated later.
         *
         * No fake metrics are generated here.
         */
        return new DashboardResult(
                configuration.getId(),
                configuration.getMaxCapacityPerSlot(),
                configuration.getBookingOrderScope().name(),
                configuration.getCheckInToleranceMinutes(),
                configuration.getPostCallToleranceMinutes(),
                configuration.getReassignmentResponseTimeoutMin(),
                configuration.getBookingCutoffTime().toString(),
                configuration.getCancellationDeadlineHours(),
                configuration.isAttendanceQueueVisible(),
                List.of(),
                false
        );
    }
}