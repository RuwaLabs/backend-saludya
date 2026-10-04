package com.ruwalabs.saludya.reassignment.application.commandservices;

import com.ruwalabs.saludya.reassignment.application.commands.AcceptReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.RejectReassignmentCommand;
import com.ruwalabs.saludya.reassignment.application.commands.SendReassignmentOfferCommand;
import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;

import java.util.Optional;

/**
 * Application service contract for reassignment commands.
 */
public interface ReassignmentCommandService {

    /**
     * Triggers the reassignment of a freed time slot.
     *
     * <p>Finds the next candidate by {@code bookingOrder} and creates a {@code PENDING}
     * offer. If there is no candidate, it does nothing (the slot stays closed).</p>
     *
     * @param command the send-reassignment-offer command
     * @return the created offer identifier, or empty if there was no candidate
     */
    Optional<Long> sendReassignmentOffer(SendReassignmentOfferCommand command);

    /**
     * Accepts a reassignment offer.
     *
     * @param command the accept command
     * @return the accepted offer, or an application error
     */
    Result<ReassignmentOffer, ApplicationError> acceptReassignment(AcceptReassignmentCommand command);

    /**
     * Rejects a reassignment offer.
     *
     * @param command the reject command
     * @return the rejected offer, or an application error
     */
    Result<ReassignmentOffer, ApplicationError> rejectReassignment(RejectReassignmentCommand command);

    /**
     * Detects accepted offers whose candidate did not arrive within the arrival window
     * (destination slot start + check-in tolerance) and marks them as no-show.
     *
     * @return the number of offers marked as no-show
     */
    int detectNoShows();
}
