package com.ruwalabs.saludya.appointments.interfaces.rest;

import com.ruwalabs.saludya.appointments.application.commands.CancelAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commandservices.AppointmentCommandService;
import com.ruwalabs.saludya.appointments.application.queryservices.AppointmentQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentsByPatientQuery;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.AppointmentResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.BookAppointmentResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.AppointmentResourceFromEntityAssembler;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.BookAppointmentCommandFromResourceAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for appointment booking and cancellation.
 */
@RestController
@RequestMapping(value = "/api/v1/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Appointments", description = "Appointment booking and cancellation endpoints")
public class AppointmentsController {

    private final AppointmentCommandService commandService;
    private final AppointmentQueryService queryService;

    public AppointmentsController(
            AppointmentCommandService commandService,
            AppointmentQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody BookAppointmentResource resource) {
        var command = BookAppointmentCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.book(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        var appointment = queryService.getById(new GetAppointmentByIdQuery(id));
        if (appointment.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Appointment", id.toString()));
        }
        return ResponseEntity.ok(
                AppointmentResourceFromEntityAssembler.toResourceFromEntity(appointment.get()));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResource>> getAppointmentsByPatient(@PathVariable Long patientId) {
        var appointments = queryService.getByPatient(new GetAppointmentsByPatientQuery(patientId));
        var resources = appointments.stream()
                .map(AppointmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        var result = commandService.cancel(new CancelAppointmentCommand(id));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
