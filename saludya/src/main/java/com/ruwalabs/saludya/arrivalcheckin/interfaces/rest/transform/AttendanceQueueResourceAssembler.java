package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.AttendanceQueue;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.AttendanceQueueResource;

/**
 * Assembler from {@link AttendanceQueue} to {@link AttendanceQueueResource}.
 */
public final class AttendanceQueueResourceAssembler {

    private AttendanceQueueResourceAssembler() {
    }

    public static AttendanceQueueResource toResource(AttendanceQueue queue) {
        return new AttendanceQueueResource(
                queue.getId(),
                queue.getIdTimeSlot(),
                queue.getDate(),
                queue.getStatus().name());
    }
}
