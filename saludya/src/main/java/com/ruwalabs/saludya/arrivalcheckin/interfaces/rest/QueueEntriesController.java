package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.DeclareAbsenceCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commandservices.CheckInCommandService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentInfo;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueueEntryByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.CheckInQueryService;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.QueueQueryService;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QueueEntryResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform.QueueEntryResourceAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for attendance queue entries.
 */
@RestController
@RequestMapping(value = "/api/v1/queue-entries", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Queue Entries", description = "Individual attendance queue entries")
public class QueueEntriesController {

    private final QueueQueryService queueQueryService;
    private final CheckInCommandService checkInCommandService;
    private final CheckInQueryService checkInQueryService;
    private final AppointmentLookupService appointmentLookupService;
    private final PatientAccessService patientAccessService;

    public QueueEntriesController(
            QueueQueryService queueQueryService,
            CheckInCommandService checkInCommandService,
            CheckInQueryService checkInQueryService,
            AppointmentLookupService appointmentLookupService,
            PatientAccessService patientAccessService) {
        this.queueQueryService = queueQueryService;
        this.checkInCommandService = checkInCommandService;
        this.checkInQueryService = checkInQueryService;
        this.appointmentLookupService = appointmentLookupService;
        this.patientAccessService = patientAccessService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a queue entry by id (patients may read their own entry)")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        var entry = queueQueryService.handle(new GetQueueEntryByIdQuery(id));
        if (entry.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var patientId = checkInQueryService.handle(new GetCheckInByIdQuery(entry.get().getIdCheckIn()))
                .flatMap(checkIn -> appointmentLookupService.findAppointment(checkIn.getIdAppointment()))
                .map(AppointmentInfo::patientId)
                .orElse(null);
        if (patientId != null && !patientAccessService.canManagePatient(patientId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("QueueEntry", "You cannot view this queue entry"));
        }
        return ResponseEntity.ok(QueueEntryResourceAssembler.toResource(entry.get()));
    }

    @PostMapping("/{id}/absent")
    @Operation(summary = "Mark a queue entry as absent")
    public ResponseEntity<?> markAbsent(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                checkInCommandService.declareAbsence(new DeclareAbsenceCommand(id, null, null)),
                ignored -> null,
                HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave the attendance queue voluntarily (patient); marks the appointment absent")
    public ResponseEntity<?> leaveQueue(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                checkInCommandService.declareAbsence(new DeclareAbsenceCommand(id, null, null)),
                ignored -> null,
                HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start the attention of a called queue entry")
    public ResponseEntity<?> startAttention(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                checkInCommandService.startAttention(id),
                QueueEntryResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finish the attention of a queue entry (marks it attended)")
    public ResponseEntity<?> finishAttention(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                checkInCommandService.finishAttention(id),
                QueueEntryResourceAssembler::toResource,
                HttpStatus.OK);
    }
}
