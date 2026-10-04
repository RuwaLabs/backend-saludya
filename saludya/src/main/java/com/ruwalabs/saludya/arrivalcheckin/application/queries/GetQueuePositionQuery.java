package com.ruwalabs.saludya.arrivalcheckin.application.queries;

/**
 * Query to get the current position of an attendance queue.
 *
 * @param attendanceQueueId the attendance queue identifier
 */
public record GetQueuePositionQuery(Long attendanceQueueId) {
}
