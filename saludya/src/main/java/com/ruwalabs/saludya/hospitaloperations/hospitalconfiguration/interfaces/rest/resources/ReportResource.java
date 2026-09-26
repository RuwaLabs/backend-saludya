package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources;

/**
 * REST representation of a generated report.
 */
public record ReportResource(
        String format,
        String generatedAt,
        String content
) {
}