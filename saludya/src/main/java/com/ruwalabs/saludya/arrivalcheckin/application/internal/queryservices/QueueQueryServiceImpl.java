package com.ruwalabs.saludya.arrivalcheckin.application.internal.queryservices;

import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetAttendanceQueueBySlotAndDateQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueueEntriesQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueueEntryByIdQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queries.GetQueuePositionQuery;
import com.ruwalabs.saludya.arrivalcheckin.application.queryservices.QueueQueryService;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueuePosition;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.AttendanceQueueRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.CheckInRepository;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.QueueEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that handles attendance queue queries.
 */
@Service
public class QueueQueryServiceImpl implements QueueQueryService {

    private final QueueEntryRepository queueEntryRepository;
    private final CheckInRepository checkInRepository;
    private final AttendanceQueueRepository attendanceQueueRepository;

    public QueueQueryServiceImpl(
            QueueEntryRepository queueEntryRepository,
            CheckInRepository checkInRepository,
            AttendanceQueueRepository attendanceQueueRepository) {
        this.queueEntryRepository = queueEntryRepository;
        this.checkInRepository = checkInRepository;
        this.attendanceQueueRepository = attendanceQueueRepository;
    }

    @Override
    public Optional<QueueEntry> handle(GetQueueEntryByIdQuery query) {
        return queueEntryRepository.findById(query.id());
    }

    @Override
    public List<QueueEntry> handle(GetQueueEntriesQuery query) {
        return queueEntryRepository.findByAttendanceQueueOrderByPosition(query.attendanceQueueId());
    }

    @Override
    public Optional<QueuePosition> handle(GetQueuePositionQuery query) {
        return queueEntryRepository.findFirstByQueueAndStatusOrderByPosition(
                        query.attendanceQueueId(), QueueEntryStatus.WAITING)
                .map(entry -> new QueuePosition(
                        entry.getPosition(),
                        (int) queueEntryRepository.countByAttendanceQueue(query.attendanceQueueId())));
    }

    @Override
    public Optional<AttendanceQueue> handle(GetAttendanceQueueBySlotAndDateQuery query) {
        return attendanceQueueRepository.findByTimeSlotAndDate(query.timeSlotId(), query.date());
    }

    @Override
    public Optional<QueuePosition> getPositionByAppointment(Long appointmentId) {
        return checkInRepository.findByAppointmentId(appointmentId)
                .flatMap(checkIn -> queueEntryRepository.findByCheckInId(checkIn.getId()))
                .map(entry -> new QueuePosition(
                        entry.getPosition(),
                        (int) queueEntryRepository.countByAttendanceQueue(entry.getIdAttendanceQueue())));
    }
}
