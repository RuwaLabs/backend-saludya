package com.ruwalabs.saludya.arrivalcheckin.domain.repositories;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for {@link QueueEntry} entities.
 */
public interface QueueEntryRepository {

    QueueEntry save(QueueEntry entry);

    Optional<QueueEntry> findById(Long id);

    /**
     * Finds the queue entry created by a check-in.
     *
     * @param checkInId the check-in identifier
     * @return the queue entry, if present
     */
    Optional<QueueEntry> findByCheckInId(Long checkInId);

    List<QueueEntry> findByAttendanceQueueOrderByPosition(Long attendanceQueueId);

    Optional<QueueEntry> findFirstByQueueAndStatusOrderByPosition(Long attendanceQueueId, QueueEntryStatus status);

    long countByAttendanceQueue(Long attendanceQueueId);

    List<QueueEntry> findByStatusAndCalledAtBefore(QueueEntryStatus status, Instant threshold);
}
