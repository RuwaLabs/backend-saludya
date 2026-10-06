package com.ruwalabs.saludya.arrivalcheckin.application.internal.commandservices;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.DeclareAbsenceCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commands.RegisterCheckInByBookingCodeCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commands.RegisterCheckInCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commandservices.CheckInCommandService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentInfo;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.NotificationService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.QrTokenService;
import com.ruwalabs.saludya.arrivalcheckin.application.model.CheckInResult;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.CheckInCompletedEvent;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.PatientAbsentEvent;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.factories.CheckInFactory;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.AttendanceQueueRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.CheckInRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.QueueEntryRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.services.QueueDomainService;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Application service that executes check-in and absence commands.
 */
@Service
public class CheckInCommandServiceImpl implements CheckInCommandService {

    private final CheckInRepository checkInRepository;
    private final AttendanceQueueRepository attendanceQueueRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final QrTokenService qrTokenService;
    private final AppointmentLookupService appointmentLookupService;
    private final HospitalConfigurationService hospitalConfigurationService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final PatientAccessService patientAccessService;
    private final QueueDomainService queueDomainService = new QueueDomainService();

    public CheckInCommandServiceImpl(
            CheckInRepository checkInRepository,
            AttendanceQueueRepository attendanceQueueRepository,
            QueueEntryRepository queueEntryRepository,
            QrTokenService qrTokenService,
            AppointmentLookupService appointmentLookupService,
            HospitalConfigurationService hospitalConfigurationService,
            NotificationService notificationService,
            ApplicationEventPublisher eventPublisher,
            PatientAccessService patientAccessService) {
        this.checkInRepository = checkInRepository;
        this.attendanceQueueRepository = attendanceQueueRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.qrTokenService = qrTokenService;
        this.appointmentLookupService = appointmentLookupService;
        this.hospitalConfigurationService = hospitalConfigurationService;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
        this.patientAccessService = patientAccessService;
    }

    @Override
    @Transactional
    public Result<CheckInResult, ApplicationError> registerCheckIn(RegisterCheckInCommand command) {
        var payload = qrTokenService.validateQrToken(command.qrToken());
        if (payload.isEmpty()) {
            return Result.failure(ApplicationError.validationError(
                    "qrToken", "Invalid or expired QR token"));
        }
        return registerCheckInForAppointment(payload.get().appointmentId(), command.qrToken());
    }

    @Override
    @Transactional
    public Result<CheckInResult, ApplicationError> registerCheckInByBookingCode(
            RegisterCheckInByBookingCodeCommand command) {
        var appointment = appointmentLookupService.findByBookingCode(command.bookingCode().trim());
        if (appointment.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Appointment", command.bookingCode()));
        }
        return registerCheckInForAppointment(appointment.get().appointmentId(), null);
    }

    private Result<CheckInResult, ApplicationError> registerCheckInForAppointment(
            Long appointmentId, String qrReference) {
        if (checkInRepository.existsByAppointmentId(appointmentId)) {
            return Result.failure(ApplicationError.conflict(
                    "CheckIn", "Appointment has already checked in"));
        }

        var appointment = appointmentLookupService.findAppointment(appointmentId);
        if (appointment.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "Appointment", appointmentId.toString()));
        }
        var info = appointment.get();

        var now = Instant.now();
        if (!queueDomainService.isWithinToleranceWindow(
                now, info.slotStart(), hospitalConfigurationService.checkInToleranceMinutes())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "check-in-tolerance", "Check-in is outside the tolerance window"));
        }

        var checkIn = checkInRepository.save(
                CheckInFactory.createCheckIn(appointmentId, qrReference));

        var today = LocalDate.now();
        var queue = attendanceQueueRepository.findByTimeSlotAndDate(info.timeSlotId(), today)
                .orElseGet(() -> attendanceQueueRepository.save(
                        AttendanceQueue.open(info.timeSlotId(), today)));

        var total = queueEntryRepository.countByAttendanceQueue(queue.getId());
        var position = queueDomainService.calculateNextPosition(total);
        var entry = queueEntryRepository.save(
                QueueEntry.create(queue.getId(), checkIn.getId(), position));

        appointmentLookupService.markPresent(appointmentId);

        eventPublisher.publishEvent(new CheckInCompletedEvent(
                checkIn.getId(), appointmentId, queue.getId(), position, now));
        notificationService.notifyCheckInCompleted(appointmentId, position);

        return Result.success(new CheckInResult(checkIn, entry, position, (int) total + 1));
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> declareAbsence(DeclareAbsenceCommand command) {
        var entry = queueEntryRepository.findById(command.queueEntryId());
        if (entry.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "QueueEntry", command.queueEntryId().toString()));
        }
        var queueEntry = entry.get();

        var appointmentId = command.appointmentId() != null ? command.appointmentId()
                : checkInRepository.findById(queueEntry.getIdCheckIn())
                        .map(CheckIn::getIdAppointment)
                        .orElse(null);

        var patientId = appointmentId == null ? null
                : appointmentLookupService.findAppointment(appointmentId)
                        .map(AppointmentInfo::patientId)
                        .orElse(null);
        if (patientId == null || !patientAccessService.canManagePatient(patientId)) {
            return Result.failure(ApplicationError.forbidden(
                    "QueueEntry", "You cannot leave this queue entry"));
        }

        queueEntry.markAsAbsent();
        queueEntryRepository.save(queueEntry);

        var freedTimeSlotId = command.freedTimeSlotId() != null ? command.freedTimeSlotId()
                : (appointmentId == null ? null
                        : appointmentLookupService.findAppointment(appointmentId)
                                .map(AppointmentInfo::timeSlotId)
                                .orElse(null));

        if (appointmentId != null) {
            appointmentLookupService.markAbsent(appointmentId);
        }

        eventPublisher.publishEvent(new PatientAbsentEvent(
                queueEntry.getId(),
                appointmentId,
                freedTimeSlotId,
                Instant.now()));
        return Result.success(null);
    }

    @Override
    @Transactional
    public int detectAbsences() {
        var threshold = Instant.now()
                .minusSeconds(hospitalConfigurationService.postCallToleranceMinutes() * 60L);
        var entries = queueEntryRepository.findByStatusAndCalledAtBefore(QueueEntryStatus.CALLED, threshold);
        for (var entry : entries) {
            entry.markAsAbsent();
            queueEntryRepository.save(entry);

            var appointmentId = checkInRepository.findById(entry.getIdCheckIn())
                    .map(CheckIn::getIdAppointment)
                    .orElse(null);
            var freedTimeSlotId = appointmentId == null ? null
                    : appointmentLookupService.findAppointment(appointmentId)
                            .map(AppointmentInfo::timeSlotId)
                            .orElse(null);

            if (appointmentId != null) {
                appointmentLookupService.markAbsent(appointmentId);
            }

            eventPublisher.publishEvent(new PatientAbsentEvent(
                    entry.getId(), appointmentId, freedTimeSlotId, Instant.now()));
        }
        return entries.size();
    }

    @Override
    @Transactional
    public Result<QueueEntry, ApplicationError> startAttention(Long queueEntryId) {
        var entry = queueEntryRepository.findById(queueEntryId);
        if (entry.isEmpty()) {
            return Result.failure(ApplicationError.notFound("QueueEntry", queueEntryId.toString()));
        }
        try {
            entry.get().startAttention();
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.businessRuleViolation("queue-entry-state", ex.getMessage()));
        }
        return Result.success(queueEntryRepository.save(entry.get()));
    }

    @Override
    @Transactional
    public Result<QueueEntry, ApplicationError> finishAttention(Long queueEntryId) {
        var entry = queueEntryRepository.findById(queueEntryId);
        if (entry.isEmpty()) {
            return Result.failure(ApplicationError.notFound("QueueEntry", queueEntryId.toString()));
        }
        var queueEntry = entry.get();
        try {
            queueEntry.markAsAttended();
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.businessRuleViolation("queue-entry-state", ex.getMessage()));
        }
        var saved = queueEntryRepository.save(queueEntry);
        checkInRepository.findById(queueEntry.getIdCheckIn())
                .map(CheckIn::getIdAppointment)
                .ifPresent(appointmentLookupService::markAttended);
        return Result.success(saved);
    }
}
