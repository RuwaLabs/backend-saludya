package com.ruwalabs.saludya.reassignment.application.queries;

import java.util.Objects;

/**
 * Query to retrieve a reassignment offer by its identifier.
 *
 * @param offerId the offer identifier
 */
public record GetOfferByIdQuery(Long offerId) {

    public GetOfferByIdQuery {
        Objects.requireNonNull(offerId, "offerId must not be null");
    }
}
