package com.ruwalabs.saludya.appointments.interfaces.rest;

import com.ruwalabs.saludya.appointments.application.commandservices.TimeSlotCommandService;
import com.ruwalabs.saludya.appointments.application.queryservices.TimeSlotQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAvailableTimeSlotsBySpecialtyQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetTimeSlotByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetTimeSlotsByDoctorAndDateQuery;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.CreateTimeSlotResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.TimeSlotResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.UpdateTimeSlotCapacityResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.UpdateTimeSlotResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.CreateTimeSlotCommandFromResourceAssembler;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.TimeSlotResourceFromEntityAssembler;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.UpdateTimeSlotCapacityCommandFromResourceAssembler;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.UpdateTimeSlotCommandFromResourceAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for time slot availability and management.
 */
@RestController
@RequestMapping(value = "/api/v1/time-slots", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Time Slots", description = "Time slot availability and management endpoints")
public class TimeSlotsController {

    private final TimeSlotCommandService commandService;
    private final TimeSlotQueryService queryService;

    public TimeSlotsController(
            TimeSlotCommandService commandService,
            TimeSlotQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<TimeSlotResource>> getAvailableTimeSlots(
            @RequestParam Long specialtyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var timeSlots = queryService.getAvailableBySpecialty(
                new GetAvailableTimeSlotsBySpecialtyQuery(specialtyId, date));
        var resources = timeSlots.stream()
                .map(TimeSlotResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotResource>> getTimeSlotsByDoctorAndDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var timeSlots = queryService.getByDoctorAndDate(
                new GetTimeSlotsByDoctorAndDateQuery(doctorId, date));
        var resources = timeSlots.stream()
                .map(TimeSlotResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTimeSlotById(@PathVariable Long id) {
        var timeSlot = queryService.getById(new GetTimeSlotByIdQuery(id));
        if (timeSlot.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("TimeSlot", id.toString()));
        }
        return ResponseEntity.ok(
                TimeSlotResourceFromEntityAssembler.toResourceFromEntity(timeSlot.get()));
    }

    @PostMapping
    public ResponseEntity<?> createTimeSlot(@Valid @RequestBody CreateTimeSlotResource resource) {
        var command = CreateTimeSlotCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.create(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                TimeSlotResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}/capacity")
    public ResponseEntity<?> updateTimeSlotCapacity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeSlotCapacityResource resource) {
        var command = UpdateTimeSlotCapacityCommandFromResourceAssembler
                .toCommandFromResource(id, resource);
        var result = commandService.updateCapacity(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                TimeSlotResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a time slot (doctor, schedule and status)")
    public ResponseEntity<?> updateTimeSlot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeSlotResource resource) {
        var command = UpdateTimeSlotCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = commandService.update(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                TimeSlotResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK);
    }
}
