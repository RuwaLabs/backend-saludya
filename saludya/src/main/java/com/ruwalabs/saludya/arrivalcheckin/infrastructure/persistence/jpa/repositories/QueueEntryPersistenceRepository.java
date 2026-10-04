package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities.QueueEntryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for queue entry persistence entities.
 */
@Repository
public interface QueueEntryPersistenceRepository extends JpaRepository<QueueEntryPersistenceEntity, Long> {

    List<QueueEntryPersistenceEntity> findAllByIdAttendanceQueueOrderByPositionAsc(Long idAttendanceQueue);

    Optional<QueueEntryPersistenceEntity> findFirstByIdAttendanceQueueAndStatusOrderByPositionAsc(
            Long idAttendanceQueue, QueueEntryStatus status);

    @Query("SELECT q FROM QueueEntryPersistenceEntity q WHERE q.idCheckIn = :idCheckIn")
    Optional<QueueEntryPersistenceEntity> findByIdCheckIn(@Param("idCheckIn") Long idCheckIn);

    long countByIdAttendanceQueue(Long idAttendanceQueue);

    List<QueueEntryPersistenceEntity> findAllByStatusAndCalledAtBefore(QueueEntryStatus status, Instant threshold);
}
