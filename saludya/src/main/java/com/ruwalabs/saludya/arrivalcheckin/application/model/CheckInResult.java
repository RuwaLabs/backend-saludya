package com.ruwalabs.saludya.arrivalcheckin.application.model;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;

/**
 * Result of a successful check-in: the check-in, its queue entry and the position.
 *
 * @param checkIn     the created check-in
 * @param queueEntry  the created queue entry
 * @param position    the patient's position in the queue
 * @param totalInQueue the total number of entries in the queue
 */
public record CheckInResult(
        CheckIn checkIn,
        QueueEntry queueEntry,
        int position,
        int totalInQueue) {
}
