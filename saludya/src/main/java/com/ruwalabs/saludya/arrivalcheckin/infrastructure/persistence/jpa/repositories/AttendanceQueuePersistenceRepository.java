package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities.AttendanceQueuePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Spring Data repository for attendance queue persistence entities.
 */
@Repository
public interface AttendanceQueuePersistenceRepository
        extends JpaRepository<AttendanceQueuePersistenceEntity, Long> {

    Optional<AttendanceQueuePersistenceEntity> findByIdTimeSlotAndDate(Long idTimeSlot, LocalDate date);
}
