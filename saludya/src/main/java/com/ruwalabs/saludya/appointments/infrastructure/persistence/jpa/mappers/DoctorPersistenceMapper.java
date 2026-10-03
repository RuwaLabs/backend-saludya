package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.DoctorJpaEntity;

/**
 * Maps between the {@link Doctor} domain entity and its JPA entity.
 */
public final class DoctorPersistenceMapper {

    private DoctorPersistenceMapper() {
    }

    public static DoctorJpaEntity toJpaEntity(Doctor domain) {
        DoctorJpaEntity entity = new DoctorJpaEntity();
        entity.setId(domain.getId());
        entity.setSpecialtyId(domain.getSpecialtyId());
        entity.setName(domain.getName());
        entity.setLastname(domain.getLastname());
        return entity;
    }

    public static Doctor toDomain(DoctorJpaEntity entity) {
        return Doctor.rehydrate(
                entity.getId(),
                entity.getSpecialtyId(),
                entity.getName(),
                entity.getLastname()
        );
    }
}
