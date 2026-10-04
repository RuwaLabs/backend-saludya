package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.assemblers;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities.QueueEntryPersistenceEntity;

/**
 * Static assembler between the {@code QueueEntry} entity and its persistence entity.
 */
public final class QueueEntryPersistenceAssembler {

    private QueueEntryPersistenceAssembler() {
    }

    public static QueueEntry toDomainFromPersistence(QueueEntryPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new QueueEntry(
                entity.getId(),
                entity.getIdAttendanceQueue(),
                entity.getIdCheckIn(),
                entity.getPosition(),
                entity.getStatus(),
                entity.getCalledAt(),
                entity.getAttendedAt());
    }

    public static QueueEntryPersistenceEntity toPersistenceFromDomain(QueueEntry entry) {
        if (entry == null) {
            return null;
        }
        var entity = new QueueEntryPersistenceEntity();
        if (entry.getId() != null) {
            entity.setId(entry.getId());
        }
        entity.setIdAttendanceQueue(entry.getIdAttendanceQueue());
        entity.setIdCheckIn(entry.getIdCheckIn());
        entity.setPosition(entry.getPosition());
        entity.setStatus(entry.getStatus());
        entity.setCalledAt(entry.getCalledAt());
        entity.setAttendedAt(entry.getAttendedAt());
        return entity;
    }
}
