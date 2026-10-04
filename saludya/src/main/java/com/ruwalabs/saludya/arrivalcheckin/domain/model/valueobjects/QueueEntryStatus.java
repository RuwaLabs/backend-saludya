package com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects;

/**
 * Status of an entry in the attendance queue.
 */
public enum QueueEntryStatus {
    WAITING,
    CALLED,
    IN_ATTENTION,
    ATTENDED,
    ABSENT
}
