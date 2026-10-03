package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;
import com.ruwalabs.saludya.appointments.domain.model.repositories.SpecialtyRepository;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.SpecialtyJpaEntity;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers.SpecialtyPersistenceMapper;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataSpecialtyJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing {@link SpecialtyRepository} with Spring Data JPA.
 */
@Component
public class SpecialtyPersistenceAdapter implements SpecialtyRepository {

    private final SpringDataSpecialtyJpaRepository repository;

    public SpecialtyPersistenceAdapter(SpringDataSpecialtyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Specialty save(Specialty specialty) {
        SpecialtyJpaEntity entity = SpecialtyPersistenceMapper.toJpaEntity(specialty);
        SpecialtyJpaEntity saved = repository.save(entity);
        return SpecialtyPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Specialty> findById(Long id) {
        return repository.findById(id).map(SpecialtyPersistenceMapper::toDomain);
    }

    @Override
    public List<Specialty> findAll() {
        return repository.findAll().stream()
                .map(SpecialtyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
