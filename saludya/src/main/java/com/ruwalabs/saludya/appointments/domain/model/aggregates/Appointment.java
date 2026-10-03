package com.ruwalabs.saludya.appointments.domain.model.aggregates;

import com.ruwalabs.saludya.appointments.domain.model.events.AppointmentAbsentEvent;
import com.ruwalabs.saludya.appointments.domain.model.events.AppointmentCancelledEvent;
import com.ruwalabs.saludya.appointments.domain.model.exceptions.AppointmentNotActiveException;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.BookingOrder;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Aggregate root representing a booked medical appointment.
 *
 * <p>Owns the {@link BookingOrder} and controls the full lifecycle of the
 * appointment through its state transition methods.</p>
 */
public class Appointment extends AbstractDomainAggregateRoot<Appointment> {

    private Long id;
    private final Long timeSlotId;
    private final Long patientId;
    private final BookingOrder bookingOrder;
    private AppointmentStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Appointment(
            Long id,
            Long timeSlotId,
            Long patientId,
            BookingOrder bookingOrder,
            AppointmentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.patientId = patientId;
        this.bookingOrder = bookingOrder;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new appointment in the {@code RESERVED} state.
     */
    public static Appointment create(Long timeSlotId, Long patientId, BookingOrder bookingOrder) {
        Objects.requireNonNull(timeSlotId, "timeSlotId cannot be null");
        Objects.requireNonNull(patientId, "patientId cannot be null");
        Objects.requireNonNull(bookingOrder, "bookingOrder cannot be null");
        Instant now = Instant.now();
        return new Appointment(null, timeSlotId, patientId, bookingOrder,
                AppointmentStatus.RESERVED, now, now);
    }

    /**
     * Reconstructs an existing appointment from its persisted state.
     */
    public static Appointment rehydrate(
            Long id,
            Long timeSlotId,
            Long patientId,
            BookingOrder bookingOrder,
            AppointmentStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Appointment(id, timeSlotId, patientId, bookingOrder, status,
                createdAt, updatedAt);
    }

    /**
     * Transitions from {@code RESERVED} to {@code CONFIRMED}.
     */
    public void confirm() {
        assertActive();
        if (status == AppointmentStatus.CONFIRMED) {
            return;
        }
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions to {@code CANCELLED}. Deadline validation is performed by the
     * application layer before invoking this method.
     */
    public void cancel() {
        assertActive();
        this.status = AppointmentStatus.CANCELLED;
        Instant now = Instant.now();
        this.updatedAt = now;
        registerDomainEvent(new AppointmentCancelledEvent(id, timeSlotId, now));
    }

    /**
     * Transitions to {@code ABSENT}.
     */
    public void markAsAbsent() {
        assertActive();
        this.status = AppointmentStatus.ABSENT;
        Instant now = Instant.now();
        this.updatedAt = now;
        registerDomainEvent(new AppointmentAbsentEvent(id, timeSlotId, now));
    }

    /**
     * Transitions to {@code ATTENDED}.
     */
    public void markAsAttended() {
        assertActive();
        this.status = AppointmentStatus.ATTENDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions to {@code EXPIRED}.
     */
    public void expire() {
        assertActive();
        this.status = AppointmentStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    /**
     * Returns {@code true} while the appointment can still change state.
     */
    public boolean isActive() {
        return status == AppointmentStatus.RESERVED
                || status == AppointmentStatus.CONFIRMED;
    }

    /**
     * Validates whether the appointment can still be cancelled, given the
     * scheduled start time and the cancellation deadline in hours.
     */
    public boolean canBeCancelled(Instant scheduledAt, int cancellationDeadlineHours) {
        if (!isActive()) {
            return false;
        }
        Instant cancellationCutoff = scheduledAt.minus(cancellationDeadlineHours, ChronoUnit.HOURS);
        return Instant.now().isBefore(cancellationCutoff);
    }

    private void assertActive() {
        if (!isActive()) {
            throw new AppointmentNotActiveException(id, status);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public BookingOrder getBookingOrder() {
        return bookingOrder;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
