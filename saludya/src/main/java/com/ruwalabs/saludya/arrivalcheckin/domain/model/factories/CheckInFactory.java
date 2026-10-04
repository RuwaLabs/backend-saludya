package com.ruwalabs.saludya.arrivalcheckin.domain.model.factories;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;

/**
 * Factory that encapsulates the creation of check-ins.
 */
public final class CheckInFactory {

    private CheckInFactory() {
    }

    /**
     * Creates a valid check-in for an appointment.
     *
     * @param appointmentId the appointment identifier
     * @param qrToken       the validated QR token
     * @return a new valid check-in
     */
    public static CheckIn createCheckIn(Long appointmentId, String qrToken) {
        return CheckIn.create(appointmentId, qrToken);
    }
}
