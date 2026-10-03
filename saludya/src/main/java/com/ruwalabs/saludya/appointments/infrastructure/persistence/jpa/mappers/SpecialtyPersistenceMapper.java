package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.SpecialtyJpaEntity;

/**
 * Maps between the {@link Specialty} domain entity and its JPA entity.
 */
public final class SpecialtyPersistenceMapper {

    private SpecialtyPersistenceMapper() {
    }

    public static SpecialtyJpaEntity toJpaEntity(Specialty domain) {
        SpecialtyJpaEntity entity = new SpecialtyJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    public static Specialty toDomain(SpecialtyJpaEntity entity) {
        return Specialty.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
