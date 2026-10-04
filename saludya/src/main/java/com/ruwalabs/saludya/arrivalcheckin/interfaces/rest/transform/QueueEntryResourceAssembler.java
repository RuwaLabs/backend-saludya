package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.QueueEntryResource;

/**
 * Assembler from {@link QueueEntry} to {@link QueueEntryResource}.
 */
public final class QueueEntryResourceAssembler {

    private QueueEntryResourceAssembler() {
    }

    public static QueueEntryResource toResource(QueueEntry entry) {
        return new QueueEntryResource(
                entry.getId(),
                entry.getIdAttendanceQueue(),
                entry.getIdCheckIn(),
                entry.getPosition(),
                entry.getStatus().name(),
                entry.getCalledAt(),
                entry.getAttendedAt());
    }
}
