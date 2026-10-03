package com.ruwalabs.saludya.appointments.domain.model.aggregates;

import com.ruwalabs.saludya.appointments.domain.model.exceptions.TimeSlotCancelledException;
import com.ruwalabs.saludya.appointments.domain.model.exceptions.TimeSlotFullException;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.TimeSlotStatus;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Aggregate root representing a concrete time block of a doctor on a given
 * date. Controls its capacity and availability state.
 */
public class TimeSlot extends AbstractDomainAggregateRoot<TimeSlot> {

    private Long id;
    private final Long doctorId;
    private final LocalDate date;
    private final LocalTime startHour;
    private final LocalTime endHour;
    private int maxCapacity;
    private int currentBookings;
    private TimeSlotStatus status;

    private TimeSlot(
            Long id,
            Long doctorId,
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            int maxCapacity,
            int currentBookings,
            TimeSlotStatus status
    ) {
        this.id = id;
        this.doctorId = doctorId;
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
        this.maxCapacity = maxCapacity;
        this.currentBookings = currentBookings;
        this.status = status;
    }

    /**
     * Creates a new time slot in the {@code AVAILABLE} state with no bookings.
     */
    public static TimeSlot create(
            Long doctorId,
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            int maxCapacity
    ) {
        Objects.requireNonNull(doctorId, "doctorId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(startHour, "startHour cannot be null");
        Objects.requireNonNull(endHour, "endHour cannot be null");
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive");
        }
        if (!endHour.isAfter(startHour)) {
            throw new IllegalArgumentException("endHour must be after startHour");
        }
        return new TimeSlot(null, doctorId, date, startHour, endHour, maxCapacity,
                0, TimeSlotStatus.AVAILABLE);
    }

    /**
     * Reconstructs an existing time slot from its persisted state.
     */
    public static TimeSlot rehydrate(
            Long id,
            Long doctorId,
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            int maxCapacity,
            int currentBookings,
            TimeSlotStatus status
    ) {
        return new TimeSlot(id, doctorId, date, startHour, endHour, maxCapacity,
                currentBookings, status);
    }

    /**
     * Returns {@code true} when another booking can still be accepted.
     */
    public boolean hasAvailableCapacity() {
        return status != TimeSlotStatus.CANCELLED && currentBookings < maxCapacity;
    }

    /**
     * Registers one more booking, flipping to {@code FULL} when capacity is
     * reached.
     */
    public void incrementBookings() {
        if (status == TimeSlotStatus.CANCELLED) {
            throw new TimeSlotCancelledException(id);
        }
        if (currentBookings >= maxCapacity) {
            throw new TimeSlotFullException(id);
        }
        currentBookings++;
        if (currentBookings >= maxCapacity) {
            status = TimeSlotStatus.FULL;
        }
    }

    /**
     * Releases one booking, restoring {@code AVAILABLE} when the slot was full.
     */
    public void decrementBookings() {
        if (currentBookings <= 0) {
            throw new IllegalStateException("currentBookings cannot be negative");
        }
        currentBookings--;
        if (status == TimeSlotStatus.FULL && currentBookings < maxCapacity) {
            status = TimeSlotStatus.AVAILABLE;
        }
    }

    /**
     * Returns {@code true} when the slot has reached its maximum capacity.
     */
    public boolean isFull() {
        return currentBookings >= maxCapacity;
    }

    /**
     * Returns {@code true} when the slot's end time has already passed.
     */
    public boolean isExpired() {
        return LocalDateTime.of(date, endHour).isBefore(LocalDateTime.now());
    }

    /**
     * Marks the slot as {@code CANCELLED}.
     */
    public void cancel() {
        if (status == TimeSlotStatus.CANCELLED) {
            return;
        }
        this.status = TimeSlotStatus.CANCELLED;
    }

    /**
     * Updates the maximum capacity of the slot, restoring {@code AVAILABLE}
     * when the slot was full and the new capacity exceeds current bookings.
     */
    public void updateMaxCapacity(int newMaxCapacity) {
        if (newMaxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive");
        }
        if (newMaxCapacity < currentBookings) {
            throw new IllegalArgumentException(
                    "maxCapacity cannot be less than currentBookings");
        }
        this.maxCapacity = newMaxCapacity;
        if (status == TimeSlotStatus.FULL && currentBookings < maxCapacity) {
            this.status = TimeSlotStatus.AVAILABLE;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartHour() {
        return startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public TimeSlotStatus getStatus() {
        return status;
    }
}
