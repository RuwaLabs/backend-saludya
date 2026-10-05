package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.RegisterCheckInByBookingCodeCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commands.RegisterCheckInCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commandservices.CheckInCommandService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentInfo;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.QrTokenService;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByAppointmentQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetCheckInByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.CheckInQueryService;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.QueueQueryService;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueuePosition;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.CheckInByCodeResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.CheckInQrResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.CheckInResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QrTokenResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QueuePositionResource;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform.CheckInResourceAssembler;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform.CheckInResultResourceAssembler;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform.QueuePositionResourceAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for check-ins, QR tokens and the patient's queue position.
 */
@RestController
@RequestMapping(value = "/api/v1/check-ins", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Check-ins", description = "QR check-in, digital ticket and queue position")
public class CheckInsController {

    private final CheckInCommandService checkInCommandService;
    private final CheckInQueryService checkInQueryService;
    private final QrTokenService qrTokenService;
    private final AppointmentLookupService appointmentLookupService;
    private final PatientAccessService patientAccessService;
    private final QueueQueryService queueQueryService;

    public CheckInsController(
            CheckInCommandService checkInCommandService,
            CheckInQueryService checkInQueryService,
            QrTokenService qrTokenService,
            AppointmentLookupService appointmentLookupService,
            PatientAccessService patientAccessService,
            QueueQueryService queueQueryService) {
        this.checkInCommandService = checkInCommandService;
        this.checkInQueryService = checkInQueryService;
        this.qrTokenService = qrTokenService;
        this.appointmentLookupService = appointmentLookupService;
        this.patientAccessService = patientAccessService;
        this.queueQueryService = queueQueryService;
    }

    @PostMapping("/qr")
    @Operation(summary = "Register a check-in from a scanned QR token")
    public ResponseEntity<?> registerCheckIn(@Valid @RequestBody CheckInQrResource resource) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                checkInCommandService.registerCheckIn(new RegisterCheckInCommand(resource.qrToken())),
                CheckInResultResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @PostMapping("/code")
    @Operation(summary = "Register a check-in from the reservation code (manual fallback)")
    public ResponseEntity<?> registerCheckInByCode(@Valid @RequestBody CheckInByCodeResource resource) {
        return ResponseEntityAssembler.toResponseEntityFromResult(
                checkInCommandService.registerCheckInByBookingCode(
                        new RegisterCheckInByBookingCodeCommand(resource.bookingCode())),
                CheckInResultResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get the digital ticket of a check-in")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        var checkIn = checkInQueryService.handle(new GetCheckInByIdQuery(id));
        if (checkIn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!canManageAppointment(checkIn.get().getIdAppointment())) {
            return forbidden("CheckIn", "You cannot view this check-in");
        }
        return ResponseEntity.ok(
                CheckInResourceAssembler.toResource(checkIn.get(), lookupAppointment(checkIn.get().getIdAppointment()), lookupPosition(checkIn.get().getIdAppointment())));
    }

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get the digital ticket of an appointment")
    public ResponseEntity<?> getByAppointment(@PathVariable Long appointmentId) {
        var checkIn = checkInQueryService.handle(new GetCheckInByAppointmentQuery(appointmentId));
        if (checkIn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!canManageAppointment(appointmentId)) {
            return forbidden("CheckIn", "You cannot view this check-in");
        }
        return ResponseEntity.ok(
                CheckInResourceAssembler.toResource(checkIn.get(), lookupAppointment(appointmentId), lookupPosition(appointmentId)));
    }

    private AppointmentInfo lookupAppointment(Long appointmentId) {
        return appointmentLookupService.findAppointment(appointmentId).orElse(null);
    }

    private QueuePosition lookupPosition(Long appointmentId) {
        return queueQueryService.getPositionByAppointment(appointmentId).orElse(null);
    }

    @GetMapping("/appointment/{appointmentId}/qr-token")
    @Operation(summary = "Generate the signed QR token of an appointment")
    public ResponseEntity<?> generateQrToken(@PathVariable Long appointmentId) {
        if (!canManageAppointment(appointmentId)) {
            return forbidden("Appointment", "You cannot generate the QR for this appointment");
        }
        var timeSlotId = appointmentLookupService.findAppointment(appointmentId)
                .map(info -> info.timeSlotId())
                .orElse(null);
        var token = qrTokenService.generateQrToken(appointmentId, timeSlotId);
        return ResponseEntity.ok(new QrTokenResource(token));
    }

    @GetMapping("/appointment/{appointmentId}/position")
    @Operation(summary = "Get the patient's position in the attendance queue")
    public ResponseEntity<?> getPosition(@PathVariable Long appointmentId) {
        if (!canManageAppointment(appointmentId)) {
            return forbidden("Appointment", "You cannot view the queue position of this appointment");
        }
        return queueQueryService.getPositionByAppointment(appointmentId)
                .map(QueuePositionResourceAssembler::toResource)
                .map(position -> (ResponseEntity<?>) ResponseEntity.ok(position))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean canManageAppointment(Long appointmentId) {
        var patientId = appointmentLookupService.findAppointment(appointmentId)
                .map(info -> info.patientId())
                .orElse(null);
        return patientId == null || patientAccessService.canManagePatient(patientId);
    }

    private ResponseEntity<?> forbidden(String resource, String reason) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.forbidden(resource, reason));
    }
}
