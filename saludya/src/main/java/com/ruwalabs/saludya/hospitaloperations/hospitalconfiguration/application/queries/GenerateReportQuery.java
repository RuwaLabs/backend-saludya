package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries;

import java.time.LocalDate;

/**
 * Query used to request an operational report.
 */
public record GenerateReportQuery(
        LocalDate from,
        LocalDate to,
        ReportFormat format
) {

    public GenerateReportQuery {
        if (from == null) {
            throw new IllegalArgumentException("Report start date cannot be null");
        }

        if (to == null) {
            throw new IllegalArgumentException("Report end date cannot be null");
        }

        if (to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "Report end date cannot be before start date"
            );
        }

        if (format == null) {
            throw new IllegalArgumentException("Report format cannot be null");
        }
    }

    public enum ReportFormat {
        JSON,
        CSV
    }
}