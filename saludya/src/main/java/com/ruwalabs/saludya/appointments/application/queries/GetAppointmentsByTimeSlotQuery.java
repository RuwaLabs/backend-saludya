package com.ruwalabs.saludya.appointments.application.queries;

/**
 * Query to fetch all appointments of a time slot.
 */
public record GetAppointmentsByTimeSlotQuery(Long timeSlotId) {

    public GetAppointmentsByTimeSlotQuery {
        if (timeSlotId == null || timeSlotId <= 0) {
            throw new IllegalArgumentException("timeSlotId must be positive");
        }
    }
}
