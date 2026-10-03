package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;
import com.ruwalabs.saludya.appointments.domain.model.repositories.DoctorRepository;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.DoctorJpaEntity;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers.DoctorPersistenceMapper;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataDoctorJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing {@link DoctorRepository} with Spring Data JPA.
 */
@Component
public class DoctorPersistenceAdapter implements DoctorRepository {

    private final SpringDataDoctorJpaRepository repository;

    public DoctorPersistenceAdapter(SpringDataDoctorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Doctor save(Doctor doctor) {
        DoctorJpaEntity entity = DoctorPersistenceMapper.toJpaEntity(doctor);
        DoctorJpaEntity saved = repository.save(entity);
        return DoctorPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Doctor> findById(Long id) {
        return repository.findById(id).map(DoctorPersistenceMapper::toDomain);
    }

    @Override
    public List<Doctor> findBySpecialtyId(Long specialtyId) {
        return repository.findBySpecialtyId(specialtyId).stream()
                .map(DoctorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Doctor> findAll() {
        return repository.findAll().stream()
                .map(DoctorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
