package com.ruwalabs.saludya.reassignment.application.commands;

import java.util.Objects;

/**
 * Command to reject a reassignment offer.
 *
 * @param offerId the identifier of the offer to reject
 */
public record RejectReassignmentCommand(Long offerId) {

    public RejectReassignmentCommand {
        Objects.requireNonNull(offerId, "offerId must not be null");
    }
}
