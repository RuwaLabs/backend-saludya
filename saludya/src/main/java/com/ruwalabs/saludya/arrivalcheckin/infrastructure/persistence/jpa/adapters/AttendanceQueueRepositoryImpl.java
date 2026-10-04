package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.adapters;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.AttendanceQueueRepository;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.assemblers.AttendanceQueuePersistenceAssembler;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.repositories.AttendanceQueuePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository adapter that bridges the {@code AttendanceQueueRepository} port with Spring Data JPA.
 */
@Repository
public class AttendanceQueueRepositoryImpl implements AttendanceQueueRepository {

    private final AttendanceQueuePersistenceRepository persistenceRepository;

    public AttendanceQueueRepositoryImpl(AttendanceQueuePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public AttendanceQueue save(AttendanceQueue queue) {
        var saved = persistenceRepository.save(
                AttendanceQueuePersistenceAssembler.toPersistenceFromDomain(queue));
        return AttendanceQueuePersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<AttendanceQueue> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(AttendanceQueuePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<AttendanceQueue> findByTimeSlotAndDate(Long timeSlotId, LocalDate date) {
        return persistenceRepository.findByIdTimeSlotAndDate(timeSlotId, date)
                .map(AttendanceQueuePersistenceAssembler::toDomainFromPersistence);
    }
}
