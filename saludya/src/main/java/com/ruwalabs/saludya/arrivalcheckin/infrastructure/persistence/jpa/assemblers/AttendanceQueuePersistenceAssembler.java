package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.assemblers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities.AttendanceQueuePersistenceEntity;

/**
 * Static assembler between the {@code AttendanceQueue} aggregate and its persistence entity.
 */
public final class AttendanceQueuePersistenceAssembler {

    private AttendanceQueuePersistenceAssembler() {
    }

    public static AttendanceQueue toDomainFromPersistence(AttendanceQueuePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AttendanceQueue(
                entity.getId(),
                entity.getIdTimeSlot(),
                entity.getDate(),
                entity.getStatus());
    }

    public static AttendanceQueuePersistenceEntity toPersistenceFromDomain(AttendanceQueue queue) {
        if (queue == null) {
            return null;
        }
        var entity = new AttendanceQueuePersistenceEntity();
        if (queue.getId() != null) {
            entity.setId(queue.getId());
        }
        entity.setIdTimeSlot(queue.getIdTimeSlot());
        entity.setDate(queue.getDate());
        entity.setStatus(queue.getStatus());
        return entity;
    }
}
