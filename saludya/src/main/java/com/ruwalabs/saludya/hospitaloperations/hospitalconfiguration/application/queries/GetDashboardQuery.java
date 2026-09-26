package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries;

import java.time.LocalDate;

/**
 * Query used to retrieve the operational dashboard.
 */
public record GetDashboardQuery(
        LocalDate date
) {

    public GetDashboardQuery {
        if (date == null) {
            throw new IllegalArgumentException("Dashboard date cannot be null");
        }
    }
}