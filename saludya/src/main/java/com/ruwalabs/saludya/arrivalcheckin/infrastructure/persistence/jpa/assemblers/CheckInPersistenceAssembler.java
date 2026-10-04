package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.assemblers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities.CheckInPersistenceEntity;

/**
 * Static assembler between the {@code CheckIn} aggregate and its persistence entity.
 */
public final class CheckInPersistenceAssembler {

    private CheckInPersistenceAssembler() {
    }

    public static CheckIn toDomainFromPersistence(CheckInPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CheckIn(
                entity.getId(),
                entity.getIdAppointment(),
                entity.getQrToken(),
                entity.getStatus(),
                entity.getCheckedInAt());
    }

    public static CheckInPersistenceEntity toPersistenceFromDomain(CheckIn checkIn) {
        if (checkIn == null) {
            return null;
        }
        var entity = new CheckInPersistenceEntity();
        if (checkIn.getId() != null) {
            entity.setId(checkIn.getId());
        }
        entity.setIdAppointment(checkIn.getIdAppointment());
        entity.setQrToken(checkIn.getQrToken());
        entity.setStatus(checkIn.getStatus());
        entity.setCheckedInAt(checkIn.getCheckedInAt());
        return entity;
    }
}
