package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.reports;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GenerateReportQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import org.springframework.stereotype.Component;

/**
 * Adapter responsible for generating operational reports.
 *
 * Current implementation provides CSV output without introducing
 * additional third-party dependencies.
 */
@Component
public class ReportGeneratorAdapter {

    public String generate(
            HospitalConfiguration configuration,
            GenerateReportQuery query
    ) {
        return switch (query.format()) {
            case CSV -> generateCsv(configuration);
            case JSON -> generateJson(configuration);
        };
    }

    private String generateCsv(
            HospitalConfiguration configuration
    ) {
        StringBuilder csv = new StringBuilder();

        csv.append("field,value\n");
        csv.append("id,")
                .append(configuration.getId())
                .append('\n');

        csv.append("max_capacity_per_slot,")
                .append(configuration.getMaxCapacityPerSlot())
                .append('\n');

        csv.append("booking_order_scope,")
                .append(configuration.getBookingOrderScope())
                .append('\n');

        csv.append("check_in_tolerance_minutes,")
                .append(configuration.getCheckInToleranceMinutes())
                .append('\n');

        csv.append("post_call_tolerance_minutes,")
                .append(configuration.getPostCallToleranceMinutes())
                .append('\n');

        csv.append("reassignment_response_timeout_min,")
                .append(configuration.getReassignmentResponseTimeoutMin())
                .append('\n');

        csv.append("booking_cutoff_time,")
                .append(configuration.getBookingCutoffTime())
                .append('\n');

        csv.append("cancellation_deadline_hours,")
                .append(configuration.getCancellationDeadlineHours())
                .append('\n');

        csv.append("attendance_queue_visible,")
                .append(configuration.isAttendanceQueueVisible())
                .append('\n');

        csv.append("updated_at,")
                .append(configuration.getUpdatedAt())
                .append('\n');

        return csv.toString();
    }

    private String generateJson(
            HospitalConfiguration configuration
    ) {
        return """
                {
                  "id": %d,
                  "maxCapacityPerSlot": %d,
                  "bookingOrderScope": "%s",
                  "checkInToleranceMinutes": %d,
                  "postCallToleranceMinutes": %d,
                  "reassignmentResponseTimeoutMin": %d,
                  "bookingCutoffTime": "%s",
                  "cancellationDeadlineHours": %d,
                  "attendanceQueueVisible": %s,
                  "updatedAt": "%s"
                }
                """.formatted(
                configuration.getId(),
                configuration.getMaxCapacityPerSlot(),
                configuration.getBookingOrderScope(),
                configuration.getCheckInToleranceMinutes(),
                configuration.getPostCallToleranceMinutes(),
                configuration.getReassignmentResponseTimeoutMin(),
                configuration.getBookingCutoffTime(),
                configuration.getCancellationDeadlineHours(),
                configuration.isAttendanceQueueVisible(),
                configuration.getUpdatedAt()
        );
    }
}