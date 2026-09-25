package com.ruwalabs.saludya.reassignment.infrastructure.persistence.jpa.entities;

import com.ruwalabs.saludya.reassignment.domain.model.valueobjects.ReassignmentStatus;
import com.ruwalabs.saludya.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA persistence entity for reassignment offers.
 *
 * <p>Maps the {@code ReassignmentOffer} aggregate to the {@code reassignment_offers}
 * table. References to {@code appointments} and {@code time_slots} are stored as plain
 * {@code Long} columns to keep the {@code Reassignment} context decoupled from the
 * {@code Appointments & Booking} context (no JPA relationships across contexts).</p>
 */
@Entity
@Table(name = "reassignment_offers")
@Getter
@Setter
@NoArgsConstructor
public class ReassignmentOfferPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "original_appointment_id", nullable = false)
    private Long originalAppointmentId;

    @Column(name = "freed_time_slot_id", nullable = false)
    private Long freedTimeSlotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReassignmentStatus status;

    @Column(name = "offered_at", nullable = false)
    private Instant offeredAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
