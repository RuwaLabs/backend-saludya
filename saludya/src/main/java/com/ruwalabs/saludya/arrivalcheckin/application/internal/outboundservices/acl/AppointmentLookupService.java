package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * ACL port for reading and updating appointment data owned by the
 * {@code Appointments & Booking} bounded context.
 */
public interface AppointmentLookupService {

    /**
     * Finds the appointment information for a given appointment.
     *
     * @param appointmentId the appointment identifier
     * @return the appointment info, if present
     */
    Optional<AppointmentInfo> findAppointment(Long appointmentId);

    /**
     * Marks the appointment as present (confirmed) after a successful check-in.
     *
     * @param appointmentId the appointment identifier
     */
    void markPresent(Long appointmentId);

    /**
     * Marks the appointment as attended when the consultation finishes.
     *
     * @param appointmentId the appointment identifier
     */
    void markAttended(Long appointmentId);

    /**
     * Marks the appointment as absent.
     *
     * @param appointmentId the appointment identifier
     */
    void markAbsent(Long appointmentId);
}
