package com.ruwalabs.saludya.appointments.interfaces.rest;

import com.ruwalabs.saludya.appointments.application.commands.CancelAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commandservices.AppointmentCommandService;
import com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.appointments.application.queryservices.AppointmentQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetAppointmentsByPatientQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetFilteredAppointmentsQuery;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.AppointmentResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.BookAppointmentResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.AppointmentResourceFromEntityAssembler;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.BookAppointmentCommandFromResourceAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for appointment booking, cancellation and queries.
 */
@RestController
@RequestMapping(value = "/api/v1/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Appointments", description = "Appointment booking, cancellation and query endpoints")
public class AppointmentsController {

    private final AppointmentCommandService commandService;
    private final AppointmentQueryService queryService;
    private final PatientAccessService patientAccessService;

    public AppointmentsController(
            AppointmentCommandService commandService,
            AppointmentQueryService queryService,
            PatientAccessService patientAccessService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.patientAccessService = patientAccessService;
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

    @GetMapping
    @Operation(summary = "List appointments filtered by patient, slot, doctor, specialty, date and status")
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long timeSlotId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status) {
        if (isPatient() && patientId == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.validationError("patientId", "patientId is required to list appointments"));
        }
        if (patientId != null && !patientAccessService.canManagePatient(patientId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Patient", "You cannot view appointments for this patient"));
        }
        var appointments = queryService.getFiltered(new GetFilteredAppointmentsQuery(
                patientId, timeSlotId, doctorId, specialtyId, date, status));
        var resources = appointments.stream()
                .map(AppointmentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    private boolean isPatient() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_PATIENT".equals(authority.getAuthority()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        var appointment = queryService.getById(new GetAppointmentByIdQuery(id));
        if (appointment.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Appointment", id.toString()));
        }
        if (!patientAccessService.canManagePatient(appointment.get().getPatientId())) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Appointment", "You cannot view this appointment"));
        }
        return ResponseEntity.ok(
                AppointmentResourceFromEntityAssembler.toResourceFromEntity(appointment.get()));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) String status) {
        if (!patientAccessService.canManagePatient(patientId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Patient", "You cannot view appointments for this patient"));
        }
        var appointments = queryService.getFiltered(new GetFilteredAppointmentsQuery(
                patientId, null, null, null, null, status));
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
