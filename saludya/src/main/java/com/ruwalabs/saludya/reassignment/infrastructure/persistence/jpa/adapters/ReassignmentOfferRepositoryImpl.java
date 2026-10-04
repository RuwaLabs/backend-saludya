package com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.adapters;

import com.ruwalabs.saludya.reassignment.domain.model.aggregates.ReassignmentOffer;
import com.ruwalabs.saludya.reassignment.domain.model.valueobjects.ReassignmentStatus;
import com.ruwalabs.saludya.reassignment.domain.repositories.ReassignmentOfferRepository;
import com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.assemblers.ReassignmentOfferPersistenceAssembler;
import com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.repositories.ReassignmentOfferPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the reassignment offer domain repository port with
 * Spring Data JPA.
 *
 * <p>Also acts as the event-publishing boundary: after a brand-new {@link ReassignmentOffer}
 * is persisted (and its JPA-assigned id is therefore available), the
 * {@code ReassignmentOfferSentEvent} is dispatched via Spring's
 * {@link ApplicationEventPublisher}.</p>
 */
@Repository
public class ReassignmentOfferRepositoryImpl implements ReassignmentOfferRepository {

    private final ReassignmentOfferPersistenceRepository reassignmentOfferPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReassignmentOfferRepositoryImpl(
            ReassignmentOfferPersistenceRepository reassignmentOfferPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.reassignmentOfferPersistenceRepository = reassignmentOfferPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<ReassignmentOffer> findById(Long id) {
        return reassignmentOfferPersistenceRepository.findById(id)
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ReassignmentOffer> findPendingByAppointment(Long appointmentId) {
        return reassignmentOfferPersistenceRepository
                .findAllByAppointmentIdAndStatus(appointmentId, ReassignmentStatus.PENDING)
                .stream()
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<ReassignmentOffer> findAllPending() {
        return reassignmentOfferPersistenceRepository
                .findAllByStatus(ReassignmentStatus.PENDING)
                .stream()
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<ReassignmentOffer> findAcceptedByAppointment(Long appointmentId) {
        return reassignmentOfferPersistenceRepository
                .findAllByAppointmentIdAndStatus(appointmentId, ReassignmentStatus.ACCEPTED)
                .stream()
                .findFirst()
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ReassignmentOffer> findAllAccepted() {
        return reassignmentOfferPersistenceRepository
                .findAllByStatus(ReassignmentStatus.ACCEPTED)
                .stream()
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<ReassignmentOffer> findExpiredOffers() {
        return reassignmentOfferPersistenceRepository
                .findAllByStatusAndExpiresAtBefore(ReassignmentStatus.PENDING, Instant.now())
                .stream()
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<ReassignmentOffer> findAcceptedOverdue() {
        return reassignmentOfferPersistenceRepository
                .findAllByStatusAndExpiresAtBefore(ReassignmentStatus.ACCEPTED, Instant.now())
                .stream()
                .map(ReassignmentOfferPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public ReassignmentOffer save(ReassignmentOffer offer) {
        boolean isNew = offer.getId() == null;
        var savedEntity = reassignmentOfferPersistenceRepository
                .save(ReassignmentOfferPersistenceAssembler.toPersistenceFromDomain(offer));
        var savedOffer = ReassignmentOfferPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            // The Sent event needs the JPA-generated id, which is only available on the
            // reconstructed aggregate after the save completes.
            savedOffer.onOffered();
            savedOffer.domainEvents().forEach(eventPublisher::publishEvent);
            savedOffer.clearDomainEvents();
        } else {
            // State-transition events (accepted/rejected/expired) were registered on the
            // incoming aggregate before it was passed to save().
            offer.domainEvents().forEach(eventPublisher::publishEvent);
            offer.clearDomainEvents();
        }
        return savedOffer;
    }
}
