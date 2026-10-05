package com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.transform;

import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.AppointmentInfo;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.aggregates.CheckIn;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueuePosition;
import com.ruwalabs.saludya.arrivalcheckin.interfaces.rest.resources.CheckInResource;

/**
 * Assembler from {@link CheckIn} (plus appointment and queue data) to
 * {@link CheckInResource}, the patient's digital ticket.
 */
public final class CheckInResourceAssembler {

    private CheckInResourceAssembler() {
    }

    public static CheckInResource toResource(CheckIn checkIn, AppointmentInfo info, QueuePosition position) {
        return new CheckInResource(
                checkIn.getId(),
                checkIn.getIdAppointment(),
                info == null ? null : info.bookingCode(),
                position == null ? null : String.format("T-%03d", position.value()),
                info == null ? null : info.specialtyName(),
                info == null ? null : info.doctorName(),
                info == null ? null : info.room(),
                info == null ? null : info.slotStart(),
                checkIn.getStatus().name(),
                checkIn.getCheckedInAt());
    }
}
