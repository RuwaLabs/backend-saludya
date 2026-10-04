package com.ruwalabs.saludya.reassignment.domain.repositories;

import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;

import java.util.List;
import java.util.Optional;

/**
 * Reassignment offer repository port.
 *
 * <p>Defines the persistence operations required by the {@code Reassignment}
 * bounded context. The concrete implementation lives in the infrastructure layer.</p>
 */
public interface ReassignmentOfferRepository {

    /**
     * Finds an offer by its identifier.
     *
     * @param id the offer identifier
     * @return the offer, or empty if not found
     */
    Optional<ReassignmentOffer> findById(Long id);

    /**
     * Finds all pending offers associated with a candidate appointment.
     *
     * @param appointmentId the candidate patient's appointment identifier
     * @return the list of pending offers for that appointment
     */
    List<ReassignmentOffer> findPendingByAppointment(Long appointmentId);

    /**
     * Finds all pending offers.
     *
     * @return the list of pending offers
     */
    List<ReassignmentOffer> findAllPending();

    /**
     * Finds the accepted offer of a candidate appointment, if any.
     *
     * @param appointmentId the candidate appointment identifier
     * @return the accepted offer, if present
     */
    Optional<ReassignmentOffer> findAcceptedByAppointment(Long appointmentId);

    /**
     * Finds all accepted offers.
     *
     * @return the list of accepted offers
     */
    List<ReassignmentOffer> findAllAccepted();

    /**
     * Finds all pending offers whose expiry instant has already passed.
     *
     * @return the list of pending offers that should be expired
     */
    List<ReassignmentOffer> findExpiredOffers();

    /**
     * Finds all accepted offers whose window (expiry instant) has passed without the
     * candidate arriving. These should be declared as no-show.
     *
     * @return the list of accepted offers that are overdue for arrival
     */
    List<ReassignmentOffer> findAcceptedOverdue();

    /**
     * Persists (creates or updates) an offer.
     *
     * @param offer the offer to persist
     * @return the persisted offer, including any generated identifier
     */
    ReassignmentOffer save(ReassignmentOffer offer);
}
