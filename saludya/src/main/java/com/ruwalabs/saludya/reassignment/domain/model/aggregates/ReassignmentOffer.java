package com.ruwalabs.saludya.reassignment.domain.model.aggregates;

import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferAcceptedEvent;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferAttendedEvent;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferExpiredEvent;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferNoShowEvent;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferRejectedEvent;
import com.ruwalabs.saludya.reassignment.domain.model.events.ReassignmentOfferSentEvent;
import com.ruwalabs.saludya.reassignment.domain.model.valueobjects.ReassignmentStatus;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * ReassignmentOffer aggregate root.
 *
 * <p>Represents an offer of a freed time slot sent to a candidate patient of the
 * booking queue (the "cola de reserva" ordered by {@code bookingOrder}). The aggregate
 * governs the lifecycle of the offer:</p>
 * <pre>
 * PENDING → ACCEPTED → ATTENDED
 *        → ACCEPTED → ABSENT
 *        → REJECTED
 *        → EXPIRED
 * </pre>
 *
 * <p>The reassignment works as a <b>chain</b>: when the candidate accepts, they move to
 * the freed slot ({@code freedTimeSlotId}) and their original slot
 * ({@code candidateTimeSlotId}) becomes the next freed slot to be offered.</p>
 *
 * <p>References to {@code Appointment} and {@code TimeSlot} — which belong to the
 * {@code Appointments & Booking} bounded context — are kept as plain {@code Long}
 * identifiers to avoid coupling persistence between contexts. No JPA annotation is
 * present here; persistence concerns live exclusively in
 * {@code ReassignmentOfferPersistenceEntity}.</p>
 */
@Getter
public class ReassignmentOffer extends AbstractDomainAggregateRoot<ReassignmentOffer> {

    private final Long id;

    /**
     * The identifier of the candidate patient's appointment (the one that will be
     * moved to the freed slot if the offer is accepted).
     */
    private final Long appointmentId;

    /**
     * The identifier of the appointment that freed the slot (the no-show patient or,
     * in a chain, the previous candidate who moved away). Used to transfer their
     * {@code bookingOrder} to the candidate when the offer is accepted.
     */
    private final Long originalAppointmentId;

    /**
     * The identifier of the freed time slot being offered to the candidate.
     */
    private final Long freedTimeSlotId;

    /**
     * The identifier of the time slot the candidate currently occupies. If the
     * candidate accepts, this slot becomes the next freed slot in the chain.
     */
    private final Long candidateTimeSlotId;

    private ReassignmentStatus status;

    private final Instant offeredAt;

    private Instant respondedAt;

    private final Instant expiresAt;

    /**
     * Creates a reassignment offer with a fully specified state.
     *
     * <p>Used to reconstruct an offer from persistence. Required fields are validated
     * for non-null; {@code respondedAt} is allowed to be {@code null} until the
     * candidate responds.</p>
     */
    public ReassignmentOffer(
            Long id,
            Long appointmentId,
            Long originalAppointmentId,
            Long freedTimeSlotId,
            Long candidateTimeSlotId,
            ReassignmentStatus status,
            Instant offeredAt,
            Instant respondedAt,
            Instant expiresAt) {
        this.id = id;
        this.appointmentId = Objects.requireNonNull(appointmentId, "appointmentId must not be null");
        this.originalAppointmentId = Objects.requireNonNull(originalAppointmentId, "originalAppointmentId must not be null");
        this.freedTimeSlotId = Objects.requireNonNull(freedTimeSlotId, "freedTimeSlotId must not be null");
        this.candidateTimeSlotId = Objects.requireNonNull(candidateTimeSlotId, "candidateTimeSlotId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.offeredAt = Objects.requireNonNull(offeredAt, "offeredAt must not be null");
        this.respondedAt = respondedAt;
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    /**
     * Factory method that creates a new {@code PENDING} offer for a freed time slot.
     *
     * <p>The offer timestamp is set to the current instant and the expiry timestamp
     * is provided by the caller (computed by the application layer from the hospital's
     * {@code reassignmentResponseTimeoutMin} configuration). The expiry also acts as the
     * single window for the candidate to both accept and arrive.</p>
     *
     * @param appointmentId         the candidate patient's appointment identifier
     * @param originalAppointmentId the appointment that freed the slot
     * @param freedTimeSlotId       the freed time slot identifier being offered
     * @param candidateTimeSlotId   the candidate's current time slot identifier
     * @param expiresAt             the instant at which the offer window closes
     * @return a new {@code PENDING} reassignment offer
     */
    public static ReassignmentOffer offer(
            Long appointmentId,
            Long originalAppointmentId,
            Long freedTimeSlotId,
            Long candidateTimeSlotId,
            Instant expiresAt) {
        return new ReassignmentOffer(
                null,
                appointmentId,
                originalAppointmentId,
                freedTimeSlotId,
                candidateTimeSlotId,
                ReassignmentStatus.PENDING,
                Instant.now(),
                null,
                expiresAt);
    }

    /**
     * Signals that this offer has just been persisted and its identifier is available.
     *
     * <p>Called by the repository adapter after a brand-new offer has been saved.
     * Registers a {@link ReassignmentOfferSentEvent} so the infrastructure can publish
     * it to interested subscribers.</p>
     */
    public void onOffered() {
        registerDomainEvent(new ReassignmentOfferSentEvent(
                this.id,
                this.appointmentId,
                this.freedTimeSlotId,
                this.offeredAt));
    }

    /**
     * Accepts the offer, moving the candidate's appointment to the freed slot.
     *
     * @throws IllegalStateException if the offer is not {@code PENDING} or has expired
     */
    public void accept() {
        if (!isPending()) {
            throw new IllegalStateException("Reassignment offer is not pending");
        }
        if (isExpired()) {
            throw new IllegalStateException("Reassignment offer has expired");
        }
        this.status = ReassignmentStatus.ACCEPTED;
        this.respondedAt = Instant.now();
        registerDomainEvent(new ReassignmentOfferAcceptedEvent(
                this.id,
                this.appointmentId,
                this.freedTimeSlotId,
                this.originalAppointmentId,
                this.candidateTimeSlotId,
                this.respondedAt));
    }

    /**
     * Rejects the offer, keeping the candidate's original appointment intact.
     *
     * @throws IllegalStateException if the offer is not {@code PENDING}
     */
    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException("Reassignment offer is not pending");
        }
        this.status = ReassignmentStatus.REJECTED;
        this.respondedAt = Instant.now();
        registerDomainEvent(new ReassignmentOfferRejectedEvent(
                this.id,
                this.appointmentId,
                this.freedTimeSlotId,
                this.originalAppointmentId,
                this.respondedAt));
    }

    /**
     * Expires the offer because the candidate did not respond before {@code expiresAt}.
     *
     * @throws IllegalStateException if the offer is not {@code PENDING} or has not expired yet
     */
    public void expire() {
        if (!isPending()) {
            throw new IllegalStateException("Reassignment offer is not pending");
        }
        if (!isExpired()) {
            throw new IllegalStateException("Reassignment offer has not expired yet");
        }
        this.status = ReassignmentStatus.EXPIRED;
        registerDomainEvent(new ReassignmentOfferExpiredEvent(
                this.id,
                this.appointmentId,
                this.freedTimeSlotId,
                this.originalAppointmentId,
                Instant.now()));
    }

    /**
     * Marks the accepted candidate as attended.
     *
     * @throws IllegalStateException if the offer is not {@code ACCEPTED}
     */
    public void markArrived() {
        if (!isAccepted()) {
            throw new IllegalStateException("Reassignment offer is not accepted");
        }
        this.status = ReassignmentStatus.ATTENDED;
        registerDomainEvent(new ReassignmentOfferAttendedEvent(this.id, this.appointmentId, Instant.now()));
    }

    /**
     * Marks the accepted candidate as absent (no-show) because they did not arrive
     * within the arrival window. The arrival deadline is evaluated by the application
     * layer (destination slot start + check-in tolerance).
     *
     * @throws IllegalStateException if the offer is not {@code ACCEPTED}
     */
    public void markNoShow() {
        if (!isAccepted()) {
            throw new IllegalStateException("Reassignment offer is not accepted");
        }
        this.status = ReassignmentStatus.ABSENT;
        registerDomainEvent(new ReassignmentOfferNoShowEvent(
                this.id,
                this.appointmentId,
                this.freedTimeSlotId,
                Instant.now()));
    }

    /**
     * Indicates whether the offer is still awaiting a response.
     *
     * @return {@code true} if the status is {@code PENDING}
     */
    public boolean isPending() {
        return this.status == ReassignmentStatus.PENDING;
    }

    /**
     * Indicates whether the candidate accepted the offer.
     *
     * @return {@code true} if the status is {@code ACCEPTED}
     */
    public boolean isAccepted() {
        return this.status == ReassignmentStatus.ACCEPTED;
    }

    /**
     * Indicates whether the offer's window (expiry instant) has passed.
     *
     * @return {@code true} if {@code expiresAt} is before the current instant
     */
    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }
}
