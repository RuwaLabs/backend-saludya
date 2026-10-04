package com.ruwalabs.saludya.reassignment.infrastructure.acl;

import com.ruwalabs.saludya.appointments.interfaces.acl.BookingContextFacade;
import com.ruwalabs.saludya.reassignment.application.internal.outboundservices.acl.AppointmentLookupService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Real ACL adapter for {@link AppointmentLookupService} backed by the
 * {@code Appointments & Booking} facade.
 */
@Service("reassignmentAppointmentLookupService")
public class BookingAppointmentLookupService implements AppointmentLookupService {

    private final BookingContextFacade bookingContextFacade;

    public BookingAppointmentLookupService(BookingContextFacade bookingContextFacade) {
        this.bookingContextFacade = bookingContextFacade;
    }

    @Override
    public Optional<Long> findNextCandidateByBookingOrder(Long freedTimeSlotId) {
        return bookingContextFacade.findNextCandidateByBookingOrder(freedTimeSlotId);
    }

    @Override
    public Long timeSlotOfAppointment(Long appointmentId) {
        return bookingContextFacade.timeSlotOfAppointment(appointmentId).orElse(null);
    }

    @Override
    public Optional<Long> patientOfAppointment(Long appointmentId) {
        return bookingContextFacade.findAppointmentInfo(appointmentId)
                .map(BookingContextFacade.AppointmentInfo::patientId);
    }

    @Override
    public void moveAppointment(Long appointmentId, Long targetTimeSlotId) {
        bookingContextFacade.moveAppointment(appointmentId, targetTimeSlotId);
    }

    @Override
    public void markAbsent(Long appointmentId) {
        bookingContextFacade.markAbsent(appointmentId);
    }
}
