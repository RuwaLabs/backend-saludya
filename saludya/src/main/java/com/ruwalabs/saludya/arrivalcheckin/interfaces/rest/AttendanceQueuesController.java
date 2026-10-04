package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.CallNextPatientCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commandservices.QueueCommandService;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueueEntriesQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueuePositionQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.QueueQueryService;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QueueEntryResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QueuePositionResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform.QueueEntryResourceAssembler;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform.QueuePositionResourceAssembler;
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

import java.util.List;

/**
 * REST controller for attendance queues.
 */
@RestController
@RequestMapping(value = "/api/v1/attendance-queues", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Attendance Queues", description = "Attendance queue management and patient calling")
public class AttendanceQueuesController {

    private final QueueQueryService queueQueryService;
    private final QueueCommandService queueCommandService;

    public AttendanceQueuesController(
            QueueQueryService queueQueryService,
            QueueCommandService queueCommandService) {
        this.queueQueryService = queueQueryService;
        this.queueCommandService = queueCommandService;
    }

    @GetMapping("/{id}/entries")
    @Operation(summary = "List the entries of an attendance queue")
    public ResponseEntity<List<QueueEntryResource>> getEntries(@PathVariable Long id) {
        var entries = queueQueryService.handle(new GetQueueEntriesQuery(id)).stream()
                .map(QueueEntryResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}/position")
    @Operation(summary = "Get the current queue position")
    public ResponseEntity<QueuePositionResource> getPosition(@PathVariable Long id) {
        return queueQueryService.handle(new GetQueuePositionQuery(id))
                .map(QueuePositionResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/call-next")
    @Operation(summary = "Call the next waiting patient in the queue")
    public ResponseEntity<?> callNext(@PathVariable Long id) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                queueCommandService.callNextPatient(new CallNextPatientCommand(id)),
                QueueEntryResourceAssembler::toResource,
                HttpStatus.OK);
    }
}
