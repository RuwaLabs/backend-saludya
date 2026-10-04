package com.ruwalabs.saludya.arrivalcheckin.application.queries;

/**
 * Query to list the entries of an attendance queue.
 *
 * @param attendanceQueueId the attendance queue identifier
 */
public record GetQueueEntriesQuery(Long attendanceQueueId) {
}
