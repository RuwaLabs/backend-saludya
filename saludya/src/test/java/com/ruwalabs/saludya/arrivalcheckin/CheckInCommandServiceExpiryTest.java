package com.ruwalabs.saludya.arrivalcheckin;

import com.ruwalabs.saludya.arrivalcheckin.application.internal.commandservices.CheckInCommandServiceImpl;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentInfo;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentLookupService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.NotificationService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.QrTokenService;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.PatientAbsentEvent;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.AttendanceQueueStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.CheckInStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.AttendanceQueueRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.CheckInRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.QueueEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckInCommandServiceExpiryTest {

    private final CheckInRepository checkInRepository = mock(CheckInRepository.class);
    private final AttendanceQueueRepository attendanceQueueRepository = mock(AttendanceQueueRepository.class);
    private final QueueEntryRepository queueEntryRepository = mock(QueueEntryRepository.class);
    private final QrTokenService qrTokenService = mock(QrTokenService.class);
    private final AppointmentLookupService appointmentLookupService = mock(AppointmentLookupService.class);
    private final HospitalConfigurationService configurationService = mock(HospitalConfigurationService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final PatientAccessService patientAccessService = mock(PatientAccessService.class);

    private CheckInCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckInCommandServiceImpl(
                checkInRepository,
                attendanceQueueRepository,
                queueEntryRepository,
                qrTokenService,
                appointmentLookupService,
                configurationService,
                notificationService,
                eventPublisher,
                patientAccessService);
        when(configurationService.checkInToleranceMinutes()).thenReturn(15);
    }

    @Test
    void marksUnarrivedReservationsAbsentAfterTheCheckInWindow() {
        var now = Instant.now();
        var appointment = appointment(41L, 12L, "RESERVED", now.minusSeconds(16 * 60L), now.plusSeconds(10 * 60L));
        when(appointmentLookupService.findAppointmentsByDate(any(LocalDate.class))).thenReturn(List.of(appointment));
        when(checkInRepository.existsByAppointmentId(41L)).thenReturn(false);
        when(attendanceQueueRepository.findByTimeSlotAndDate(12L, LocalDate.now(ZoneId.of("America/Lima"))))
                .thenReturn(Optional.empty());

        int changes = service.closeExpiredQueues();

        assertEquals(1, changes);
        verify(appointmentLookupService).markAbsent(41L);
        verify(eventPublisher).publishEvent(any(PatientAbsentEvent.class));
    }

    @Test
    void marksRemainingQueueEntriesAbsentAndClosesTheQueueAfterBlockEnd() {
        var now = Instant.now();
        var appointment = appointment(42L, 13L, "CONFIRMED", now.minusSeconds(60 * 60L), now.minusSeconds(20 * 60L));
        var queue = new AttendanceQueue(7L, 13L, LocalDate.now(ZoneId.of("America/Lima")), AttendanceQueueStatus.OPEN);
        var waiting = new QueueEntry(10L, 7L, 20L, 1, QueueEntryStatus.WAITING, null, null);
        var called = new QueueEntry(11L, 7L, 21L, 2, QueueEntryStatus.CALLED, now.minusSeconds(2 * 60L), null);
        when(appointmentLookupService.findAppointmentsByDate(any(LocalDate.class))).thenReturn(List.of(appointment));
        when(checkInRepository.existsByAppointmentId(42L)).thenReturn(true);
        when(attendanceQueueRepository.findByTimeSlotAndDate(13L, LocalDate.now(ZoneId.of("America/Lima"))))
                .thenReturn(Optional.of(queue));
        when(queueEntryRepository.findByAttendanceQueueOrderByPosition(7L)).thenReturn(List.of(waiting, called));
        when(checkInRepository.findById(20L)).thenReturn(Optional.of(checkIn(42L)));
        when(checkInRepository.findById(21L)).thenReturn(Optional.of(checkIn(43L)));
        when(appointmentLookupService.findAppointment(42L)).thenReturn(Optional.of(appointment));
        when(appointmentLookupService.findAppointment(43L)).thenReturn(Optional.of(
                appointment(43L, 13L, "CONFIRMED", now.minusSeconds(60 * 60L), now.minusSeconds(20 * 60L))));

        int changes = service.closeExpiredQueues();

        assertEquals(3, changes);
        assertEquals(QueueEntryStatus.ABSENT, waiting.getStatus());
        assertEquals(QueueEntryStatus.ABSENT, called.getStatus());
        assertEquals(AttendanceQueueStatus.CLOSED, queue.getStatus());
        verify(appointmentLookupService).markAbsent(42L);
        verify(appointmentLookupService).markAbsent(43L);
        verify(eventPublisher, org.mockito.Mockito.times(2)).publishEvent(any(PatientAbsentEvent.class));
    }

    @Test
    void doesNotInterruptAnAttentionAlreadyInProgress() {
        var now = Instant.now();
        var appointment = appointment(44L, 14L, "CONFIRMED", now.minusSeconds(60 * 60L), now.minusSeconds(20 * 60L));
        var queue = new AttendanceQueue(8L, 14L, LocalDate.now(ZoneId.of("America/Lima")), AttendanceQueueStatus.OPEN);
        var inAttention = new QueueEntry(12L, 8L, 22L, 1, QueueEntryStatus.IN_ATTENTION, now.minusSeconds(10 * 60L), null);
        when(appointmentLookupService.findAppointmentsByDate(any(LocalDate.class))).thenReturn(List.of(appointment));
        when(checkInRepository.existsByAppointmentId(44L)).thenReturn(true);
        when(attendanceQueueRepository.findByTimeSlotAndDate(14L, LocalDate.now(ZoneId.of("America/Lima"))))
                .thenReturn(Optional.of(queue));
        when(queueEntryRepository.findByAttendanceQueueOrderByPosition(8L)).thenReturn(List.of(inAttention));

        int changes = service.closeExpiredQueues();

        assertEquals(0, changes);
        assertEquals(QueueEntryStatus.IN_ATTENTION, inAttention.getStatus());
        assertEquals(AttendanceQueueStatus.OPEN, queue.getStatus());
        verify(queueEntryRepository, never()).save(any());
    }

    private AppointmentInfo appointment(Long id, Long slotId, String status, Instant start, Instant end) {
        return new AppointmentInfo(id, slotId, 100L, status, start, end, "AVAILABLE",
                "SAL-TEST" + id, "Medicina general", "Ana Rojas", "Consultorio 01");
    }

    private CheckIn checkIn(Long appointmentId) {
        return new CheckIn(appointmentId + 100, appointmentId, null, CheckInStatus.VALID, Instant.now());
    }
}
