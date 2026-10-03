package com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * Port for reading appointment data owned by the {@code Appointments & Booking}
 * bounded context.
 *
 * <p>The reassignment works over the "cola de reserva" (booking queue): candidates are
 * ordered by {@code bookingOrder}, not by next time slot. The concrete implementation
 * will call the Booking context facade; for now an in-memory stub is provided so the
 * Reassignment context can run independently.</p>
 */
public interface AppointmentLookupService {

    /**
     * Finds the next candidate appointment for a freed slot, ordered by {@code bookingOrder}.
     *
     * <p>The specialty is derived internally from the freed slot by the underlying
     * context (Booking). Candidates already in the freed slot are excluded.</p>
     *
     * @param freedTimeSlotId the freed time slot identifier
     * @return the candidate appointment id with the lowest {@code bookingOrder}, or empty if none
     */
    Optional<Long> findNextCandidateByBookingOrder(Long freedTimeSlotId);

    /**
     * Returns the time slot currently occupied by a given appointment.
     *
     * <p>Used to fill the {@code candidateTimeSlotId} of the offer: it becomes the next
     * freed slot in the chain when the candidate accepts.</p>
     *
     * @param appointmentId the candidate patient's appointment identifier
     * @return the time slot identifier of that appointment
     */
    Long timeSlotOfAppointment(Long appointmentId);
}
