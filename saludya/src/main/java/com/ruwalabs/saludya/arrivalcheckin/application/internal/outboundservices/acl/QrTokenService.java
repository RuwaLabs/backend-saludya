package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * Outbound port for generating and validating the signed QR check-in tokens.
 */
public interface QrTokenService {

    /**
     * Generates a signed QR token for an appointment.
     *
     * @param appointmentId the appointment identifier
     * @param timeSlotId    the time slot identifier
     * @return the compact JWT to be encoded in the QR
     */
    String generateQrToken(Long appointmentId, Long timeSlotId);

    /**
     * Validates a QR token and extracts its payload.
     *
     * @param token the raw token
     * @return the payload if the token is valid, empty otherwise
     */
    Optional<QrTokenPayload> validateQrToken(String token);
}
