package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.commands.UpdateConfigurationCommand;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GenerateReportQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.queries.GetDashboardQuery;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services.ConfigurationCommandService;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services.ConfigurationQueryService;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.ConfigurationResource;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.DashboardResource;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.ReportResource;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.resources.UpdateConfigurationResource;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.interfaces.rest.transform.ConfigurationResourceAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * REST controller for Hospital Operations & Configuration.
 */
@RestController
@RequestMapping("/api/v1/config")
public class ConfigurationController {

    private final ConfigurationCommandService commandService;
    private final ConfigurationQueryService queryService;
    private final ConfigurationResourceAssembler assembler;

    public ConfigurationController(
            ConfigurationCommandService commandService,
            ConfigurationQueryService queryService,
            ConfigurationResourceAssembler assembler
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<ConfigurationResource> getConfiguration() {
        return ResponseEntity.ok(
                assembler.toResource(
                        queryService.getConfiguration()
                )
        );
    }

    @PutMapping
    public ResponseEntity<ConfigurationResource> updateConfiguration(
            @RequestBody UpdateConfigurationResource resource
    ) {
        UpdateConfigurationCommand command =
                new UpdateConfigurationCommand(
                        resource.maxCapacityPerSlot(),
                        resource.bookingOrderScope(),
                        resource.checkInToleranceMinutes(),
                        resource.postCallToleranceMinutes(),
                        resource.reassignmentResponseTimeoutMin(),
                        resource.bookingCutoffTime(),
                        resource.cancellationDeadlineHours(),
                        resource.attendanceQueueVisible()
                );

        return ResponseEntity.ok(
                assembler.toResource(
                        commandService.updateConfiguration(command)
                )
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResource> getDashboard(
            @RequestParam(required = false) LocalDate date
    ) {
        LocalDate dashboardDate =
                Optional.ofNullable(date)
                        .orElse(LocalDate.now());

        GetDashboardQuery query =
                new GetDashboardQuery(dashboardDate);

        return ResponseEntity.ok(
                assembler.toResource(
                        queryService.getDashboard(query)
                )
        );
    }

    @GetMapping("/reports")
    public ResponseEntity<ReportResource> generateReport(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(
                    defaultValue = "JSON"
            ) GenerateReportQuery.ReportFormat format
    ) {
        GenerateReportQuery query =
                new GenerateReportQuery(
                        from,
                        to,
                        format
                );

        return ResponseEntity.ok(
                assembler.toResource(
                        queryService.generateReport(query)
                )
        );
    }
}