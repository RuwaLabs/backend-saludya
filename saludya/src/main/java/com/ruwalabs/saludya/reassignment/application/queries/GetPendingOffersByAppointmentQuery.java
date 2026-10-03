package com.ruwalabs.saludya.reassignment.application.queries;

import java.util.Objects;

/**
 * Query to retrieve the pending offers of a candidate appointment.
 *
 * @param appointmentId the candidate patient's appointment identifier
 */
public record GetPendingOffersByAppointmentQuery(Long appointmentId) {

    public GetPendingOffersByAppointmentQuery {
        Objects.requireNonNull(appointmentId, "appointmentId must not be null");
    }
}
