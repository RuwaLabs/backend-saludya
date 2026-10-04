package com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects;

/**
 * Queue position value object.
 *
 * <p>Encapsulates the patient's position in the attendance queue together with
 * the total number of patients waiting.</p>
 *
 * @param value        the patient's position (1-based)
 * @param totalInQueue the total number of entries in the queue
 */
public record QueuePosition(int value, int totalInQueue) {

    public QueuePosition {
        if (value < 0) {
            throw new IllegalArgumentException("Queue position must not be negative");
        }
        if (totalInQueue < 0) {
            throw new IllegalArgumentException("Total in queue must not be negative");
        }
    }
}
