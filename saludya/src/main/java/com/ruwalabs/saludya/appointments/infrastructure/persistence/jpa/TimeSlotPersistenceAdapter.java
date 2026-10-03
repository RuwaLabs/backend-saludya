package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.TimeSlotStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.TimeSlotJpaEntity;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers.TimeSlotPersistenceMapper;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataTimeSlotJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing {@link TimeSlotRepository} with Spring Data JPA.
 */
@Component
public class TimeSlotPersistenceAdapter implements TimeSlotRepository {

    private final SpringDataTimeSlotJpaRepository repository;

    public TimeSlotPersistenceAdapter(SpringDataTimeSlotJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        TimeSlotJpaEntity entity = TimeSlotPersistenceMapper.toJpaEntity(timeSlot);
        TimeSlotJpaEntity saved = repository.save(entity);
        return TimeSlotPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<TimeSlot> findById(Long id) {
        return repository.findById(id).map(TimeSlotPersistenceMapper::toDomain);
    }

    @Override
    public List<TimeSlot> findAvailableBySpecialty(Long specialtyId, LocalDate date) {
        return repository.findAvailableBySpecialty(specialtyId, date, TimeSlotStatus.AVAILABLE).stream()
                .map(TimeSlotPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<TimeSlot> findByDoctorAndDate(Long doctorId, LocalDate date) {
        return repository.findByDoctorIdAndDate(doctorId, date).stream()
                .map(TimeSlotPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
