package com.ruwalabs.saludya.reassignment.application.commands;

import java.util.Objects;

/**
 * Command to accept a reassignment offer.
 *
 * @param offerId the identifier of the offer to accept
 */
public record AcceptReassignmentCommand(Long offerId) {

    public AcceptReassignmentCommand {
        Objects.requireNonNull(offerId, "offerId must not be null");
    }
}
