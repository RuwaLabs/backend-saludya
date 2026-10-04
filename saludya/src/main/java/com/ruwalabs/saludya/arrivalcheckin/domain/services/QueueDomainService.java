package com.ruwalabs.saludya.arrivalcheckin.domain.services;

import java.time.Instant;

/**
 * Domain service that encapsulates the attendance-queue rules that do not belong
 * to a single aggregate: position calculation and tolerance-window validation.
 */
public class QueueDomainService {

    /**
     * Calculates the next position in a queue.
     *
     * @param currentEntries the number of entries currently in the queue
     * @return the next 1-based position
     */
    public int calculateNextPosition(long currentEntries) {
        return (int) currentEntries + 1;
    }

    /**
     * Validates whether a check-in instant falls within the tolerance window that
     * starts at the time slot start.
     *
     * @param checkInTime     the instant of the check-in
     * @param slotStart       the instant the time slot starts
     * @param toleranceMinutes the tolerance window in minutes
     * @return {@code true} if the check-in is within the window
     */
    public boolean isWithinToleranceWindow(Instant checkInTime, Instant slotStart, int toleranceMinutes) {
        var deadline = slotStart.plusSeconds(toleranceMinutes * 60L);
        return !checkInTime.isAfter(deadline);
    }
}
