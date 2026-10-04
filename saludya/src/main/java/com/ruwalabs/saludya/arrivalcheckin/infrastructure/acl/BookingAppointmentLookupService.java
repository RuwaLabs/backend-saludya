package com.ruwalabs.saludya.arrivalcheckin.infrastructure.acl;

import com.ruwalabs.saludya.appointments.interfaces.acl.BookingContextFacade;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentInfo;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentLookupService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Real ACL adapter for {@link AppointmentLookupService} backed by the
 * {@code Appointments & Booking} facade.
 */
@Service("arrivalAppointmentLookupService")
public class BookingAppointmentLookupService implements AppointmentLookupService {

    private final BookingContextFacade bookingContextFacade;

    public BookingAppointmentLookupService(BookingContextFacade bookingContextFacade) {
        this.bookingContextFacade = bookingContextFacade;
    }

    @Override
    public Optional<AppointmentInfo> findAppointment(Long appointmentId) {
        return bookingContextFacade.findAppointmentInfo(appointmentId)
                .map(info -> new AppointmentInfo(
                        info.id(), info.timeSlotId(), info.patientId(), info.status(), info.slotStart()));
    }

    @Override
    public void markPresent(Long appointmentId) {
        bookingContextFacade.markPresent(appointmentId);
    }

    @Override
    public void markAttended(Long appointmentId) {
        bookingContextFacade.markAttended(appointmentId);
    }

    @Override
    public void markAbsent(Long appointmentId) {
        bookingContextFacade.markAbsent(appointmentId);
    }
}
