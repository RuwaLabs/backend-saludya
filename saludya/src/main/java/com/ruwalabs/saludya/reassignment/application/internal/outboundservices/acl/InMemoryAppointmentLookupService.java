package com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * In-memory stub for {@link AppointmentLookupService}.
 *
 * <p>TODO: replace with the real implementation that calls the
 * {@code BookingsContextFacade} once the Appointments &amp; Booking context is ready.</p>
 */
@Service
public class InMemoryAppointmentLookupService implements AppointmentLookupService {

    @Override
    public Optional<Long> findNextCandidateByBookingOrder(Long freedTimeSlotId) {
        // Placeholder: always returns a fixed candidate for now.
        return Optional.of(999L);
    }

    @Override
    public Long timeSlotOfAppointment(Long appointmentId) {
        // Placeholder: always returns a fixed time slot for now.
        return 888L;
    }
}
