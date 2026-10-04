package com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.reassignment.domain.model.valueobjects.ReassignmentStatus;
import com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.entities.ReassignmentOfferPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data repository for reassignment offer persistence entities.
 */
@Repository
public interface ReassignmentOfferPersistenceRepository
        extends JpaRepository<ReassignmentOfferPersistenceEntity, Long> {

    /**
     * Finds all offers of a given status associated with a candidate appointment.
     *
     * @param appointmentId the candidate patient's appointment identifier
     * @param status        the offer status
     * @return the list of matching offers
     */
    List<ReassignmentOfferPersistenceEntity> findAllByAppointmentIdAndStatus(Long appointmentId, ReassignmentStatus status);

    /**
     * Finds all offers of a given status.
     *
     * @param status the offer status
     * @return the list of matching offers
     */
    List<ReassignmentOfferPersistenceEntity> findAllByStatus(ReassignmentStatus status);

    /**
     * Finds all offers of a given status whose expiry instant is before the given instant.
     *
     * @param status the offer status
     * @param now    the reference instant
     * @return the list of offers that have expired
     */
    List<ReassignmentOfferPersistenceEntity> findAllByStatusAndExpiresAtBefore(ReassignmentStatus status, Instant now);
}
