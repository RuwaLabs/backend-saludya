package com.ruwalabs.saludya.reassignment.application.queryservices;

import com.ruwalabs.saludya.reassignment.application.queries.GetOfferByIdQuery;
import com.ruwalabs.saludya.reassignment.application.queries.GetPendingOffersByAppointmentQuery;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for reassignment read queries.
 */
public interface ReassignmentQueryService {

    /**
     * Retrieves a reassignment offer by its identifier.
     *
     * @param query the get-by-id query
     * @return the offer, or empty if not found
     */
    Optional<ReassignmentOffer> handle(GetOfferByIdQuery query);

    /**
     * Retrieves the pending offers of a candidate appointment.
     *
     * @param query the get-pending query
     * @return the list of pending offers for that appointment
     */
    List<ReassignmentOffer> handle(GetPendingOffersByAppointmentQuery query);

    /**
     * Retrieves all pending offers whose candidate appointment belongs to a patient
     * managed by the authenticated caller. Used by the app as the reassignment
     * notification list.
     *
     * @return the list of pending offers for the current user
     */
    List<ReassignmentOffer> getPendingForCurrentUser();
}
