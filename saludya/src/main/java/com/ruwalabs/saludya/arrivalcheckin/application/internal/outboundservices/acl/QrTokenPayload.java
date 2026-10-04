package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

/**
 * Payload extracted from a validated QR token.
 *
 * @param appointmentId the appointment identifier
 * @param timeSlotId    the time slot identifier
 */
public record QrTokenPayload(Long appointmentId, Long timeSlotId) {
}
