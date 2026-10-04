package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.adapters;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.CheckInRepository;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.assemblers.CheckInPersistenceAssembler;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.repositories.CheckInPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository adapter that bridges the {@code CheckInRepository} port with Spring Data JPA.
 */
@Repository
public class CheckInRepositoryImpl implements CheckInRepository {

    private final CheckInPersistenceRepository persistenceRepository;

    public CheckInRepositoryImpl(CheckInPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public CheckIn save(CheckIn checkIn) {
        var saved = persistenceRepository.save(
                CheckInPersistenceAssembler.toPersistenceFromDomain(checkIn));
        return CheckInPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public Optional<CheckIn> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(CheckInPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<CheckIn> findByAppointmentId(Long appointmentId) {
        return persistenceRepository.findByIdAppointment(appointmentId)
                .map(CheckInPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByAppointmentId(Long appointmentId) {
        return persistenceRepository.existsByIdAppointment(appointmentId);
    }
}
