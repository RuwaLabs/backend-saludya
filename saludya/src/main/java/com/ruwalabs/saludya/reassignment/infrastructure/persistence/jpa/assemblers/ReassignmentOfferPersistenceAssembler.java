package com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.assemblers;

import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.entities.ReassignmentOfferPersistenceEntity;

/**
 * Static assembler between the {@code ReassignmentOffer} domain aggregate and its
 * persistence representation.
 */
public final class ReassignmentOfferPersistenceAssembler {

    private ReassignmentOfferPersistenceAssembler() {
    }

    /**
     * Reconstructs a domain aggregate from a persistence entity.
     *
     * @param entity the persistence entity
     * @return the corresponding domain aggregate, or {@code null} if the entity is {@code null}
     */
    public static ReassignmentOffer toDomainFromPersistence(ReassignmentOfferPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ReassignmentOffer(
                entity.getId(),
                entity.getAppointmentId(),
                entity.getOriginalAppointmentId(),
                entity.getFreedTimeSlotId(),
                entity.getCandidateTimeSlotId(),
                entity.getStatus(),
                entity.getOfferedAt(),
                entity.getRespondedAt(),
                entity.getExpiresAt());
    }

    /**
     * Maps a domain aggregate to a persistence entity.
     *
     * <p>The identifier is copied only when the aggregate already carries one (an
     * update). For new aggregates, the identifier is left {@code null} so JPA
     * generates it.</p>
     *
     * @param offer the domain aggregate
     * @return the corresponding persistence entity, or {@code null} if the aggregate is {@code null}
     */
    public static ReassignmentOfferPersistenceEntity toPersistenceFromDomain(ReassignmentOffer offer) {
        if (offer == null) {
            return null;
        }
        var entity = new ReassignmentOfferPersistenceEntity();
        if (offer.getId() != null) {
            entity.setId(offer.getId());
        }
        entity.setAppointmentId(offer.getAppointmentId());
        entity.setOriginalAppointmentId(offer.getOriginalAppointmentId());
        entity.setFreedTimeSlotId(offer.getFreedTimeSlotId());
        entity.setCandidateTimeSlotId(offer.getCandidateTimeSlotId());
        entity.setStatus(offer.getStatus());
        entity.setOfferedAt(offer.getOfferedAt());
        entity.setRespondedAt(offer.getRespondedAt());
        entity.setExpiresAt(offer.getExpiresAt());
        return entity;
    }
}
