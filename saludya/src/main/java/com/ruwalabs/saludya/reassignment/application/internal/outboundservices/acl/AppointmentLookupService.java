package com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * ACL port for reading and updating appointment data owned by the
 * {@code Appointments & Booking} bounded context.
 */
public interface AppointmentLookupService {

    /**
     * Finds the next candidate appointment for a freed slot, ordered by {@code bookingOrder}.
     *
     * @param freedTimeSlotId      the freed time slot identifier
     * @param excludeAppointmentId an appointment to exclude (e.g. the one that just moved), nullable
     * @return the candidate appointment id with the lowest {@code bookingOrder}, or empty if none
     */
    Optional<Long> findNextCandidateByBookingOrder(Long freedTimeSlotId, Long excludeAppointmentId);

    /**
     * Returns the time slot currently occupied by a given appointment.
     *
     * @param appointmentId the candidate patient's appointment identifier
     * @return the time slot identifier of that appointment
     */
    Long timeSlotOfAppointment(Long appointmentId);

    /**
     * Returns the patient profile that owns a given appointment.
     *
     * @param appointmentId the appointment identifier
     * @return the patient identifier, if the appointment exists
     */
    Optional<Long> patientOfAppointment(Long appointmentId);

    /**
     * Returns the instant the appointment's time slot starts.
     *
     * @param appointmentId the appointment identifier
     * @return the slot start instant, if available
     */
    Optional<java.time.Instant> slotStartOfAppointment(Long appointmentId);

    /**
     * Returns the current status of the appointment (e.g. RESERVED, CONFIRMED, ATTENDED).
     *
     * @param appointmentId the appointment identifier
     * @return the status name, if the appointment exists
     */
    Optional<String> statusOfAppointment(Long appointmentId);

    /**
     * Moves the candidate's appointment to the freed slot.
     *
     * @param appointmentId    the candidate appointment identifier
     * @param targetTimeSlotId the freed time slot identifier
     */
    void moveAppointment(Long appointmentId, Long targetTimeSlotId);

    /**
     * Marks the candidate's appointment as absent (accepted but never arrived).
     *
     * @param appointmentId the candidate appointment identifier
     */
    void markAbsent(Long appointmentId);
}
