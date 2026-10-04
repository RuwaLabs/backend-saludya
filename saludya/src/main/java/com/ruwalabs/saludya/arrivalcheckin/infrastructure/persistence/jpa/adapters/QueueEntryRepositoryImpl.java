package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.adapters;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.QueueEntryRepository;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.assemblers.QueueEntryPersistenceAssembler;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.repositories.QueueEntryPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the {@code QueueEntryRepository} port with Spring Data JPA.
 */
@Repository
public class QueueEntryRepositoryImpl implements QueueEntryRepository {

    private final QueueEntryPersistenceRepository persistenceRepository;

    public QueueEntryRepositoryImpl(QueueEntryPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public QueueEntry save(QueueEntry entry) {
        var saved = persistenceRepository.save(
                QueueEntryPersistenceAssembler.toPersistenceFromDomain(entry));
        return QueueEntryPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<QueueEntry> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(QueueEntryPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<QueueEntry> findByCheckInId(Long checkInId) {
        return persistenceRepository.findByIdCheckIn(checkInId)
                .map(QueueEntryPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<QueueEntry> findByAttendanceQueueOrderByPosition(Long attendanceQueueId) {
        return persistenceRepository.findAllByIdAttendanceQueueOrderByPositionAsc(attendanceQueueId).stream()
                .map(QueueEntryPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<QueueEntry> findFirstByQueueAndStatusOrderByPosition(Long attendanceQueueId, QueueEntryStatus status) {
        return persistenceRepository.findFirstByIdAttendanceQueueAndStatusOrderByPositionAsc(attendanceQueueId, status)
                .map(QueueEntryPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public long countByAttendanceQueue(Long attendanceQueueId) {
        return persistenceRepository.countByIdAttendanceQueue(attendanceQueueId);
    }

    @Override
    public List<QueueEntry> findByStatusAndCalledAtBefore(QueueEntryStatus status, Instant threshold) {
        return persistenceRepository.findAllByStatusAndCalledAtBefore(status, threshold).stream()
                .map(QueueEntryPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
